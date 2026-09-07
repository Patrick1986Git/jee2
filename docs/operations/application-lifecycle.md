# Application lifecycle and replica termination

## Application contract

Enterprise Shop supports one or more replicas but does not own a production load balancer or deployment manifest. The application contract is:

- `GET /actuator/health/liveness` reports only Spring application liveness. A shared PostgreSQL outage must not turn this signal down and trigger replica restart loops.
- Under the `prod` profile, `GET /actuator/health/readiness` combines Spring readiness with the standard `db` health contributor. A replica is therefore not eligible for business traffic while it is starting, shutting down, or unable to reach PostgreSQL. Optional SMTP delivery and Stripe are not readiness dependencies.
- Health details remain governed by `management.endpoint.health.show-details=when_authorized`. The health endpoint and its component paths retain the existing anonymous access policy; privileged Actuator endpoints remain ADMIN-only.
- `GET /actuator/health` remains the aggregate operational health endpoint. Docker Compose uses it because the local full-stack smoke check is expected to fail when its PostgreSQL service is unavailable. Production traffic routing must use readiness rather than aggregate health or liveness.

Spring Boot 4.1.1 defaults to graceful embedded-server shutdown. The repository makes that choice explicit and retains the framework's 30-second timeout for each lifecycle shutdown phase. On `SIGTERM`, Spring publishes `REFUSING_TRAFFIC`, rejects new server work, and gives already accepted HTTP requests up to the web graceful-shutdown phase timeout to finish. The executable-form Docker `ENTRYPOINT` makes the JVM the container process, so it receives the signal directly.

The 30-second value is a per-phase bound, not a total process-shutdown deadline or a guarantee that every external operation completes. SMTP connect, read, and write timeouts are each bounded at 30 seconds and shorter than the notification claim. Stripe SDK calls can outlive a phase timeout. If an HTTP or worker operation does not finish, the deployment may force termination after Spring's bounded lifecycle; the durable idempotency, transaction, claim, retry, and reconciliation rules below provide safety.

The relevant shutdown phases run from highest to lowest. Web graceful shutdown runs at phase `2147482623`, web-server stop at `2147481599`, and the auto-configured scheduler and executor infrastructure at phase `1073741823`. The web graceful phase and scheduler phase can each legitimately consume the configured 30 seconds; web-server stop between them is synchronous and is not expected to add another asynchronous wait. Scheduler early shutdown begins at context close, so its work can finish while HTTP requests drain, but that overlap is not guaranteed: an in-flight task can remain when the later scheduler phase is reached and make that phase wait.

Docker Compose therefore grants the application 65 seconds after its stop signal: at most 30 seconds for web request draining, at most 30 additional seconds for the later scheduler phase, and a five-second margin for the synchronous web-server stop, remaining bean destruction, and JVM exit. This is the intended complete-lifecycle allowance for the lifecycle topology currently present in the application, not a hard guarantee against a stuck destruction callback and not a generic formula for future `SmartLifecycle` additions. At 65 seconds Compose may force termination and durable recovery applies. Any new materially blocking lifecycle phase must revisit both this derivation and the Compose grace period. A production deployment must separately add its own load-balancer polling and deregistration latency before its unconditional-kill deadline.

## Deployment-owned rolling sequence

For replacement of replica A while replica B remains available, the deployment platform must:

1. poll A's readiness endpoint and route traffic only while it returns success;
2. initiate normal termination with `SIGTERM` and stop routing new traffic to A when readiness becomes unavailable;
3. allow for health-check propagation and connection draining at the external edge;
4. allow for both bounded Spring lifecycle phases and ordinary process exit before forcing termination; and
5. retain at least one ready replica, such as B, throughout the rollout.

Spring can expose state and drain requests already admitted to Tomcat, but it cannot remove A from an external routing table. The deployment must account for its own polling interval and deregistration latency in addition to the application's multi-phase lifecycle allowance. Authenticated reads, checkout, order mutations, payment-intent creation, webhooks, and ADMIN commands all use the same server drain boundary; there is no endpoint-specific shutdown bypass.

## Scheduled workers

Spring's auto-configured scheduler is a lifecycle-managed `ThreadPoolTaskScheduler`. On context close its early-shutdown signal stops accepting/starting scheduled work, but does not itself prove that a currently executing invocation has completed. The scheduler's coordinated stop occurs later, in phase `1073741823`, and can wait up to that phase's separate 30-second timeout for the invocation. It does not enable `spring.task.scheduling.shutdown.await-termination`: enabling that mode would defer scheduler shutdown and can allow scheduled triggers to continue during later context-close processing. If the scheduler phase expires, executor destruction interrupts remaining work.

The scheduler has one thread by default, so the three pollers do not execute concurrently within one replica. Multiple replicas may poll concurrently, and their existing database coordination remains the authority for ownership.

### Outbox

An outbox candidate remains `PENDING` until its handler and `markProcessed` operation commit in one transaction. A graceful stop lets the current invocation finish within the scheduler phase budget, which is distinct from the earlier web drain phase. Forced termination rolls back an incomplete transaction, leaving the event available to another poll; retry and dead-letter accounting handles committed failures. No separate in-memory claim has to expire.

### Notification delivery

Notification delivery commits a tokenized `PROCESSING` claim before provider I/O, whose transaction is separate from sending and finalization. A graceful stop gives the current batch the scheduler phase budget to finish; it does not borrow or extend the web drain phase. If termination occurs after SMTP accepted a message but before success finalization, the claim becomes eligible again after its five-minute default duration; duplicate delivery remains an unavoidable at-least-once boundary and the claim token prevents a stale worker from finalizing a newer claim. Expired claims at the attempt limit become failed rather than cycling forever.

### Reservation expiration

Reservation expiration commits a tokenized five-minute claim lease before Stripe work. Stripe calls occur outside the claim transaction, and terminal transitions are convergent. Its in-flight invocation receives the scheduler phase budget, independently of HTTP draining. If the process disappears, another replica can reclaim the work after the lease, subject to the retry delay and attempt budget. Provider idempotency and terminal-state convergence protect payment and inventory state when termination occurs between a provider response and local finalization.

## Partial failures

- **PostgreSQL outage:** aggregate health and production readiness fail; liveness remains healthy while the application process itself is viable. This drains business traffic without asking the platform to churn every replica.
- **SMTP unavailable or disabled:** it does not affect readiness or liveness. Enabled delivery is bounded by configured JavaMail timeouts and recovers through notification retries and claims.
- **Stripe unavailable:** it does not affect readiness or liveness. HTTP callers receive the operation result if it completes during draining; otherwise checkout idempotency and payment reconciliation make a later retry safe. Reservation work recovers through its lease and convergence rules.
- **Lifecycle allowance exceeded:** the deployment may force termination. Database transactions roll back, durable pending work remains, notification and reservation claims expire, and idempotent/convergent processing recovers ambiguous provider outcomes. The per-phase timeout must not be extended to the five-minute leases merely to hide forced termination; leases are recovery bounds, not drain targets.
