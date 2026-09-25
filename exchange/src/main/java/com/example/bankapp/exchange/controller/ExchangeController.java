package com.example.bankapp.exchange.controller;

import com.example.bankapp.exchange.model.Currency;
import com.example.bankapp.exchange.model.ExchangeRate;
import com.example.bankapp.exchange.service.ExchangeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@RequiredArgsConstructor
@Controller
public class ExchangeController {
    private final ExchangeService exchangeService;

    @GetMapping
    public ResponseEntity<List<ExchangeRate>> getExchangeRates() {
        return ResponseEntity.ok(exchangeService.getRates());
    }

    @GetMapping("/{currency}")
    public ResponseEntity<ExchangeRate> getExchangeRate(@PathVariable Currency currency) {
        return ResponseEntity.ok(exchangeService.getRate(currency));
    }

    @PutMapping
    public ResponseEntity<Void> setExchangeRates(@RequestBody List<@Valid ExchangeRate> rates) {
        exchangeService.setRates(rates);

        return ResponseEntity.noContent().build();
    }
}
