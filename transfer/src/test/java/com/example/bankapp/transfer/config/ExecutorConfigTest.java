package com.example.bankapp.transfer.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.Executor;

@TestConfiguration
public class ExecutorConfigTest {
    @Bean
    public Executor transferExecutor() {
        return Runnable::run;
    }
}
