package com.company.shop.persistence.migration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import com.company.shop.persistence.support.PostgresContainerSupport;

@SpringBootTest(
        classes = CheckConstraintsMigrationIT.TestConfig.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@ActiveProfiles("test")
class CheckConstraintsMigrationIT extends PostgresContainerSupport {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void migrate_shouldCreateExpectedCheckConstraintsForCorePricingAndDateIntegrity() {
        Map<String, String> constraints = jdbcTemplate.query(
                "SELECT con.conname, pg_get_constraintdef(con.oid) AS definition " +
                        "FROM pg_constraint con " +
                        "JOIN pg_namespace nsp ON nsp.oid = con.connamespace " +
                        "WHERE nsp.nspname = 'public' " +
                        "AND con.contype = 'c' " +
                        "AND con.conname IN (" +
                        "'chk_products_price_non_negative', " +
                        "'chk_products_stock_non_negative', " +
                        "'chk_orders_total_amount_non_negative', " +
                        "'chk_order_items_quantity_positive', " +
                        "'chk_order_items_price_non_negative', " +
                        "'chk_payments_amount_non_negative', " +
                        "'chk_discount_codes_used_count_non_negative', " +
                        "'chk_discount_codes_usage_limit_positive_or_null', " +
                        "'chk_discount_codes_valid_to_after_valid_from', " +
                        "'chk_product_images_sort_order_non_negative')",
                rs -> {
                    Map<String, String> result = new java.util.HashMap<>();
                    while (rs.next()) {
                        result.put(rs.getString("conname"), rs.getString("definition"));
                    }
                    return result;
                });

        assertThat(constraints)
                .hasSize(10)
                .containsKeys(
                        "chk_products_price_non_negative",
                        "chk_products_stock_non_negative",
                        "chk_orders_total_amount_non_negative",
                        "chk_order_items_quantity_positive",
                        "chk_order_items_price_non_negative",
                        "chk_payments_amount_non_negative",
                        "chk_discount_codes_used_count_non_negative",
                        "chk_discount_codes_usage_limit_positive_or_null",
                        "chk_discount_codes_valid_to_after_valid_from",
                        "chk_product_images_sort_order_non_negative");

        assertThat(constraints.get("chk_products_price_non_negative")).contains("price >= 0");
        assertThat(constraints.get("chk_products_stock_non_negative")).contains("stock >= 0");
        assertThat(constraints.get("chk_orders_total_amount_non_negative")).contains("total_amount >= 0");
        assertThat(constraints.get("chk_order_items_quantity_positive")).contains("quantity > 0");
        assertThat(constraints.get("chk_order_items_price_non_negative")).contains("price >= 0");
        assertThat(constraints.get("chk_payments_amount_non_negative")).contains("amount >= 0");
        assertThat(constraints.get("chk_discount_codes_used_count_non_negative")).contains("used_count >= 0");
        assertThat(constraints.get("chk_discount_codes_usage_limit_positive_or_null")).contains("usage_limit IS NULL").contains("usage_limit > 0");
        assertThat(constraints.get("chk_discount_codes_valid_to_after_valid_from")).contains("valid_to > valid_from");
        assertThat(constraints.get("chk_product_images_sort_order_non_negative")).contains("sort_order >= 0");
    }

    @Configuration(proxyBeanMethods = false)
    @ImportAutoConfiguration({
            DataSourceAutoConfiguration.class,
            JdbcTemplateAutoConfiguration.class,
            FlywayAutoConfiguration.class
    })
    static class TestConfig {
    }
}
