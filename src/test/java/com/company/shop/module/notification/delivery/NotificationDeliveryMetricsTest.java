package com.company.shop.module.notification.delivery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.company.shop.module.notification.entity.NotificationStatus;
import com.company.shop.module.notification.repository.NotificationRepository;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

class NotificationDeliveryMetricsTest {

    private static final Instant NOW = Instant.parse("2026-09-07T12:00:00Z");
    private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
    private final NotificationRepository repository = mock(NotificationRepository.class);
    private final SimpleMeterRegistry meters = new SimpleMeterRegistry();

    @Test
    void constructor_shouldRegisterExactUntaggedGaugeSetWithCurrentState() {
        when(repository.countActionable(NOW)).thenReturn(5L);
        when(repository.findOldestActionableAt(NOW)).thenReturn(Optional.of(NOW.minusSeconds(120)));
        when(repository.countByStatus(NotificationStatus.FAILED)).thenReturn(3L);
        when(repository.findOldestFailedAt()).thenReturn(Optional.of(NOW.minusSeconds(600)));

        new NotificationDeliveryMetrics(repository, meters, clock);

        assertGauge("shop.notification.actionable.count", 5);
        assertGauge("shop.notification.actionable.oldest.age.seconds", 120);
        assertGauge("shop.notification.failed.count", 3);
        assertGauge("shop.notification.failed.oldest.age.seconds", 600);
        assertThat(meters.getMeters()).allSatisfy(meter -> assertThat(meter.getId().getTags()).isEmpty());
    }

    @Test
    void ageGauges_shouldReturnZeroForNoWorkAndClockAnomalies() {
        when(repository.findOldestActionableAt(NOW)).thenReturn(Optional.empty());
        when(repository.findOldestFailedAt()).thenReturn(Optional.of(NOW.plusSeconds(30)));

        new NotificationDeliveryMetrics(repository, meters, clock);

        assertGauge("shop.notification.actionable.oldest.age.seconds", 0);
        assertGauge("shop.notification.failed.oldest.age.seconds", 0);
    }

    private void assertGauge(String name, double expected) {
        Gauge gauge = meters.get(name).gauge();
        assertThat(gauge.value()).isEqualTo(expected);
    }
}
