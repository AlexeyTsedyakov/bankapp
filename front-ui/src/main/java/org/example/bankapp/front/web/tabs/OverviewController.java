package org.example.bankapp.front.web.tabs;

import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest;
import lombok.RequiredArgsConstructor;
import org.example.bankapp.front.auth.UserDetailsImpl;
import org.example.bankapp.front.common.model.Currency;
import org.example.bankapp.front.integration.accounts.exceptions.AccountsServiceException;
import org.example.bankapp.front.integration.accounts.service.AccountsService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RequiredArgsConstructor
@Controller
@RequestMapping("/overview")
public class OverviewController {
    private final AccountsService accountsService;

    @HxRequest
    @GetMapping
    public String getOverviewTab(Model model, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            fillModel(model, userDetails.getId());
        } catch (AccountsServiceException e) {
            model.addAttribute("accountsServiceErrorMessage", e.getMessage());
        }

        return "fragments/tabs/overview :: overview-tab";
    }

    @HxRequest
    @PostMapping("/bank-accounts")
    public String createBankAccount(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                    @RequestParam Currency currency, Model model) {
        Long userId = userDetails.getId();
        try {
            accountsService.createBankAccount(userId, currency);
            fillModel(model, userId);
        } catch (AccountsServiceException e) {
            model.addAttribute("accountsServiceErrorMessage", e.getMessage());
        }

        return "fragments/tabs/overview :: overview-tab";
    }

    @HxRequest
    @DeleteMapping("/bank-accounts/{id}")
    public String deleteBankAccount(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                    @PathVariable Long id, Model model) {
        try {
            accountsService.deleteBankAccount(id);
            fillModel(model, userDetails.getId());
        } catch (AccountsServiceException e) {
            model.addAttribute("accountsServiceErrorMessage", e.getMessage());
        }

        return "fragments/tabs/overview :: overview-tab";
    }

    private void fillModel(Model model, Long userId) {
        var userWithBankAccounts = accountsService.getUserWithBankAccounts(userId);
        Set<Currency> availableCurrencies = accountsService.getAvailableCurrencies(userWithBankAccounts);

        model.addAttribute("bankAccounts", userWithBankAccounts.bankAccounts());
        model.addAttribute("availableCurrencies", availableCurrencies);
    }
}
