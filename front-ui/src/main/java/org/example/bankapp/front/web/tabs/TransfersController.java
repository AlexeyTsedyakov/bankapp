package org.example.bankapp.front.web.tabs;

import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.bankapp.front.auth.UserDetailsImpl;
import org.example.bankapp.front.common.exceptions.IntegrationException;
import org.example.bankapp.front.integration.accounts.AccountsGateway;
import org.example.bankapp.front.integration.accounts.dto.response.BankAccountResponse;
import org.example.bankapp.front.integration.transfer.TransferGateway;
import org.example.bankapp.front.integration.transfer.dto.TransferRequest;
import org.example.bankapp.front.web.tabs.dto.ExternalTransferDto;
import org.example.bankapp.front.web.tabs.dto.InternalTransferDto;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Controller
@RequestMapping("/transfers")
public class TransfersController {
    private final TransferGateway transferGateway;
    private final AccountsGateway accountsGateway;

    @HxRequest
    @GetMapping
    public String getTransfersTab(Model model, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        populateBankAccounts(model, userDetails.getId());
        model.addAttribute("internalTransferDto", new InternalTransferDto());
        model.addAttribute("externalTransferDto", new ExternalTransferDto());

        return "fragments/tabs/transfers :: transfers-tab";
    }

    @HxRequest
    @PostMapping("/internal")
    public String transferInternal(@Valid @ModelAttribute InternalTransferDto internalTransferDto,
                                   BindingResult bindingResult, Model model,
                                   @AuthenticationPrincipal UserDetailsImpl userDetails) {
        populateBankAccounts(model, userDetails.getId());

        if (internalTransferDto.fromAccountId().equals(internalTransferDto.toAccountId())) {
            String message = "Счета отправления и получения должны быть разными";
            bindingResult.rejectValue("fromAccountId", "same.accounts.id", message);
        }

        if (bindingResult.hasErrors()) {
            return "fragments/tabs/transfers :: internal-transfer";
        }

        TransferRequest request = TransferRequest.builder()
                .fromAccountId(internalTransferDto.fromAccountId())
                .toAccountId(internalTransferDto.toAccountId())
                .amount(internalTransferDto.amount())
                .build();
        UUID idempotencyKey = UUID.randomUUID();
        try {
            transferGateway.transfer(request, idempotencyKey);
            model.addAttribute("internalTransferSuccessMessage", "Операция обрабатывается");
        } catch (IntegrationException e) {
            model.addAttribute("internalTransferErrorMessage", e.getMessage());
        }

        return "fragments/tabs/transfers :: internal-transfer";
    }

    @HxRequest
    @PostMapping("/external")
    public String transferExternal(@Valid @ModelAttribute ExternalTransferDto externalTransferDto,
                                   BindingResult bindingResult, Model model,
                                   @AuthenticationPrincipal UserDetailsImpl userDetails) {
        populateBankAccounts(model, userDetails.getId());

        if (bindingResult.hasErrors()) {
            return "fragments/tabs/transfers :: external-transfer";
        }

        BankAccountResponse toBankAccount;
        try {
            String accountNumber = externalTransferDto.toAccountNumber();
            toBankAccount = accountsGateway.getBankAccountByNumber(accountNumber);
        } catch (IntegrationException e) {
            bindingResult.rejectValue("toAccountNumber", "account.not.found", e.getMessage());
            return "fragments/tabs/transfers :: external-transfer";
        }

        TransferRequest request = TransferRequest.builder()
                .fromAccountId(externalTransferDto.fromAccountId())
                .toAccountId(toBankAccount.id())
                .amount(externalTransferDto.amount())
                .build();
        UUID idempotencyKey = UUID.randomUUID();
        try {
            transferGateway.transfer(request, idempotencyKey);
            model.addAttribute("externalTransferSuccessMessage", "Операция обрабатывается");
        } catch (IntegrationException e) {
            model.addAttribute("externalTransferErrorMessage", e.getMessage());
        }

        return "fragments/tabs/transfers :: external-transfer";
    }

    private void populateBankAccounts(Model model, Long userId) {
        try {
            List<BankAccountResponse> bankAccounts = accountsGateway.getUserBankAccounts(userId);
            model.addAttribute("bankAccounts", bankAccounts);
        } catch (IntegrationException e) {
            model.addAttribute("accountsServiceErrorMessage", e.getMessage());
        }
    }
}
