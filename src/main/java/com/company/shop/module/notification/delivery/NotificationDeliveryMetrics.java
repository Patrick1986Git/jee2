package com.company.shop.module.notification.delivery;

import java.time.Clock;
import java.time.Duration;
import java.util.Optional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.company.shop.module.notification.entity.NotificationStatus;
import com.company.shop.module.notification.repository.NotificationRepository;

import io.micrometer.core.instrument.MeterRegistry;

@Component
@ConditionalOnProperty(name = "spring.datasource.url")
public class NotificationDeliveryMetrics {

    public NotificationDeliveryMetrics(NotificationRepository repository, MeterRegistry meters, Clock clock) {
        meters.gauge("shop.notification.actionable.count", repository,
                value -> value.countActionable(clock.instant()));
        meters.gauge("shop.notification.actionable.oldest.age.seconds", repository,
                value -> ageSeconds(value.findOldestActionableAt(clock.instant()), clock));
        meters.gauge("shop.notification.failed.count", repository,
                value -> value.countByStatus(NotificationStatus.FAILED));
        meters.gauge("shop.notification.failed.oldest.last_attempt.age.seconds", repository,
                value -> ageSeconds(value.findOldestFailedLastAttemptAt(), clock));
    }

    private static double ageSeconds(Optional<java.time.Instant> oldest, Clock clock) {
        return oldest.map(value -> (double) Math.max(0, Duration.between(value, clock.instant()).toSeconds()))
                .orElse(0.0);
    }
}
