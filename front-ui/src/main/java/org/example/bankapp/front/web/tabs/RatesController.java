package org.example.bankapp.front.web.tabs;

import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest;
import lombok.RequiredArgsConstructor;
import org.example.bankapp.front.integration.exchange.ExchangeGateway;
import org.example.bankapp.front.integration.exchange.dto.ExchangeRate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequiredArgsConstructor
@Controller
@RequestMapping("/rates")
public class RatesController {
    private final ExchangeGateway exchangeGateway;

    @HxRequest
    @GetMapping
    public String getRatesTab(Model model) {
        try {
            List<ExchangeRate> rates = exchangeGateway.getExchangeRates();
            model.addAttribute("rates", rates);
        } catch (Exception e) {
            model.addAttribute("exchangeServiceErrorMessage", e.getMessage());
            model.addAttribute("rates", List.of());
        }

        return "fragments/tabs/rates :: rates-tab";
    }
}
