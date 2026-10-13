package com.example.bankapp.transfer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
public class ExecutorConfig {
    @Bean
    public Executor transferExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
