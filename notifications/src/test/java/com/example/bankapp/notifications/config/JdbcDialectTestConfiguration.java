package com.example.bankapp.notifications.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jdbc.core.dialect.JdbcDialect;
import org.springframework.data.jdbc.core.dialect.JdbcPostgresDialect;

@TestConfiguration
public class JdbcDialectTestConfiguration {
    @Bean
    public JdbcDialect jdbcDialect() {
        return JdbcPostgresDialect.INSTANCE;
    }
}
