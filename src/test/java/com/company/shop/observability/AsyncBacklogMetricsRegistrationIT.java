package com.company.shop.observability;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.company.shop.module.notification.delivery.NotificationDeliveryMetrics;
import com.company.shop.module.order.outbox.OutboxEventMetrics;
import com.company.shop.persistence.support.PostgresContainerSupport;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;

@SpringBootTest
@ActiveProfiles("test")
class AsyncBacklogMetricsRegistrationIT extends PostgresContainerSupport {

    private static final List<String> METRIC_NAMES = List.of(
            "shop.outbox.actionable.count",
            "shop.outbox.actionable.oldest.age.seconds",
            "shop.outbox.dead_letter.count",
            "shop.outbox.dead_letter.oldest.age.seconds",
            "shop.notification.actionable.count",
            "shop.notification.actionable.oldest.age.seconds",
            "shop.notification.failed.count",
            "shop.notification.failed.oldest.last_attempt.age.seconds");

    @Autowired
    private OutboxEventMetrics outboxEventMetrics;

    @Autowired
    private NotificationDeliveryMetrics notificationDeliveryMetrics;

    @Autowired
    private MeterRegistry meterRegistry;

    @Test
    void applicationContext_shouldRegisterEachAsyncBacklogGaugeExactlyOnce() {
        assertThat(outboxEventMetrics).isNotNull();
        assertThat(notificationDeliveryMetrics).isNotNull();

        for (String metricName : METRIC_NAMES) {
            assertThat(meterRegistry.find(metricName).meters())
                    .singleElement()
                    .satisfies(meter -> {
                        assertThat(meter.getId().getName()).isEqualTo(metricName);
                        assertThat(meter.getId().getType()).isEqualTo(Meter.Type.GAUGE);
                        assertThat(meter.getId().getTags()).isEmpty();
                    });
        }
    }
}
