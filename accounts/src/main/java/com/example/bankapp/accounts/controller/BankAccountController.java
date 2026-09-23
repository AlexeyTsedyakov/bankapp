package com.example.bankapp.accounts.controller;

import com.example.bankapp.accounts.controller.dto.request.BankAccountCreateRequest;
import com.example.bankapp.accounts.controller.dto.request.BankAccountDepositRequest;
import com.example.bankapp.accounts.controller.dto.request.BankAccountWithdrawRequest;
import com.example.bankapp.accounts.controller.dto.response.BankAccountResponse;
import com.example.bankapp.accounts.mapper.BankAccountMapper;
import com.example.bankapp.accounts.model.BankAccount;
import com.example.bankapp.accounts.service.BankAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/bank-accounts")
public class BankAccountController {
    private final BankAccountMapper bankAccountMapper;
    private final BankAccountService bankAccountService;

    @PostMapping
    public ResponseEntity<BankAccountResponse> createBankAccount(@RequestBody @Valid BankAccountCreateRequest request) {
        BankAccount entity = bankAccountMapper.toEntity(request);
        BankAccount saved = bankAccountService.create(entity);
        BankAccountResponse response = bankAccountMapper.toBankAccountResponse(saved);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BankAccountResponse> getBankAccount(@PathVariable Long id) {
        BankAccount found = bankAccountService.findById(id);
        BankAccountResponse response = bankAccountMapper.toBankAccountResponse(found);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<BankAccountResponse>> getUserBankAccounts(@RequestParam Long userId) {
        List<BankAccount> found = bankAccountService.findAllByUserId(userId);
        List<BankAccountResponse> response = bankAccountMapper.toBankAccountResponseList(found);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBankAccount(@PathVariable Long id) {
        bankAccountService.delete(id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/deposit")
    public ResponseEntity<BankAccountResponse> deposit(@PathVariable Long id,
                                                       @RequestBody @Valid BankAccountDepositRequest request) {
        BankAccount updated = bankAccountService.deposit(id, request.amount());
        BankAccountResponse response = bankAccountMapper.toBankAccountResponse(updated);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/withdraw")
    public ResponseEntity<BankAccountResponse> withdraw(@PathVariable Long id,
                                                        @RequestBody @Valid BankAccountWithdrawRequest request) {
        BankAccount updated = bankAccountService.withdraw(id, request.amount());
        BankAccountResponse response = bankAccountMapper.toBankAccountResponse(updated);

        return ResponseEntity.ok(response);
    }
}
