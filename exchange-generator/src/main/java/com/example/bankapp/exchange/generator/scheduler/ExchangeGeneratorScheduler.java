package com.example.bankapp.exchange.generator.scheduler;

import com.example.bankapp.exchange.generator.integration.ExchangeGateway;
import com.example.bankapp.exchange.generator.service.ExchangeRateGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Component
public class ExchangeGeneratorScheduler {
    private final ExchangeGateway exchangeGateway;
    private final ExchangeRateGenerator rateGenerator;

    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.SECONDS)
    public void updateExchangeRates() {
        try {
            exchangeGateway.setExchangeRates(rateGenerator.generateRates());
        } catch (Exception e) {
            log.error("Error updating exchange rates: {}", e.getMessage());
        }
    }
}
