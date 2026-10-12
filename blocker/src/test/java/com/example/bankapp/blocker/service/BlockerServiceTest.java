package com.example.bankapp.blocker.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = BlockerService.class)
@ActiveProfiles("test")
public class BlockerServiceTest {
    @Autowired
    private BlockerService service;

    @Test
    void shouldBlock_oneCall_returnsFalse() {
        long accountId = 1;
        boolean result = service.shouldBlock(accountId);

        assertThat(result).isFalse();
    }

    @Test
    void shouldBlock_twoCallsInOneSecond_returnsTrue() {
        long accountId = 1;
        service.shouldBlock(accountId);
        boolean result = service.shouldBlock(accountId);

        assertThat(result).isTrue();
    }
}
