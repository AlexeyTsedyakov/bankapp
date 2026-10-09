package com.example.bankapp.cash.controller;

import com.example.bankapp.cash.controller.dto.CashDepositRequest;
import com.example.bankapp.cash.controller.dto.CashWithdrawRequest;
import com.example.bankapp.cash.integration.accounts.dto.response.BankAccountResponse;
import com.example.bankapp.cash.service.CashService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
public class CashController {
    private final CashService cashService;

    @PostMapping("/deposit")
    public ResponseEntity<BankAccountResponse> deposit(@RequestBody @Valid CashDepositRequest request,
                                                       @RequestHeader(name = "Idempotency-Key") UUID idempotencyKey) {
        BankAccountResponse response =
                cashService.deposit(request.bankAccountId(), request.amount(), idempotencyKey);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/withdraw")
    public ResponseEntity<BankAccountResponse> withdraw(@RequestBody @Valid CashWithdrawRequest request,
                                                        @RequestHeader(name = "Idempotency-Key") UUID idempotencyKey) {
        BankAccountResponse response =
                cashService.withdraw(request.bankAccountId(), request.amount(), idempotencyKey);

        return ResponseEntity.ok(response);
    }
}
