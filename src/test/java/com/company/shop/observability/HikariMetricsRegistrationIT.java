package com.company.shop.observability;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.company.shop.persistence.support.PostgresContainerSupport;

import io.micrometer.core.instrument.MeterRegistry;

@SpringBootTest
@ActiveProfiles("test")
class HikariMetricsRegistrationIT extends PostgresContainerSupport {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private MeterRegistry meterRegistry;

    @Test
    void applicationContext_shouldRegisterStandardHikariAndDataSourceMeters() throws SQLException {
        try (var connection = dataSource.getConnection()) {
            assertThat(connection.isValid(1)).isTrue();
        }

        assertThat(meterRegistry.find("hikaricp.connections.active").gauge()).isNotNull();
        assertThat(meterRegistry.find("hikaricp.connections.pending").gauge()).isNotNull();
        assertThat(meterRegistry.find("hikaricp.connections.timeout").counter()).isNotNull();
        assertThat(meterRegistry.find("jdbc.connections.active").gauge()).isNotNull();
    }
}
