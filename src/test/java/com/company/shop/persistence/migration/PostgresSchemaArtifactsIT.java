package com.company.shop.persistence.migration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import com.company.shop.persistence.support.PostgresContainerSupport;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class PostgresSchemaArtifactsIT extends PostgresContainerSupport {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void schema_shouldContainCriticalFlywayAndDomainTables() {
        Boolean flywayHistoryExists = jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM information_schema.tables " +
                        "WHERE table_schema = 'public' AND table_name = 'flyway_schema_history')",
                Boolean.class);

        Boolean productReviewsExists = jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM information_schema.tables " +
                        "WHERE table_schema = 'public' AND table_name = 'product_reviews')",
                Boolean.class);

        assertThat(flywayHistoryExists).isTrue();
        assertThat(productReviewsExists).isTrue();
    }

    @Test
    void schema_shouldContainCaseInsensitiveEmailUniquenessMechanism() {
        Boolean emailLowerUniqueIndexExists = jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM pg_indexes " +
                        "WHERE schemaname = 'public' AND tablename = 'users' " +
                        "AND indexdef ILIKE '%UNIQUE INDEX%' " +
                        "AND indexdef ILIKE '%(lower(email))%')",
                Boolean.class);

        assertThat(emailLowerUniqueIndexExists).isTrue();
    }

    @Test
    void schema_shouldContainFtsArtifactsForProductsSearch() {
        Boolean searchVectorColumnExists = jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM information_schema.columns " +
                        "WHERE table_schema = 'public' AND table_name = 'products' AND column_name = 'search_vector')",
                Boolean.class);

        Boolean ginIndexForSearchVectorExists = jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM pg_indexes " +
                        "WHERE schemaname = 'public' AND tablename = 'products' " +
                        "AND indexname = 'idx_products_search_vector' " +
                        "AND indexdef ILIKE '%USING gin%' " +
                        "AND indexdef ILIKE '%search_vector%')",
                Boolean.class);

        Boolean polishDictionaryExists = jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM pg_ts_dict WHERE dictname = 'polish_hunspell')",
                Boolean.class);

        Boolean polishConfigExists = jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM pg_ts_config WHERE cfgname = 'polish')",
                Boolean.class);

        assertThat(searchVectorColumnExists).isTrue();
        assertThat(ginIndexForSearchVectorExists).isTrue();
        assertThat(polishDictionaryExists).isTrue();
        assertThat(polishConfigExists).isTrue();
    }
}