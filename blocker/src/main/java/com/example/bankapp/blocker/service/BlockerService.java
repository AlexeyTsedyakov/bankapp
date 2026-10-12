package com.example.bankapp.blocker.service;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BlockerService {
    private final Map<Long, Instant> lastOperationsByAccountId = new ConcurrentHashMap<>();

    public boolean shouldBlock(long accountId) {
        Instant now = Instant.now();

        // Атомарно получаем предыдущее время и записываем новое
        Instant lastOperation = lastOperationsByAccountId.put(accountId, now);

        if (lastOperation == null) {
            return false;
        }

        Duration duration = Duration.between(lastOperation, now);
        return duration.toSeconds() <= 1;
    }
}
