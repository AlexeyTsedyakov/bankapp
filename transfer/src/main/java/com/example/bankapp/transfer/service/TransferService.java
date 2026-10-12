package com.example.bankapp.transfer.service;

import com.example.bankapp.transfer.exceptions.ServiceUnavailableException;
import com.example.bankapp.transfer.integration.accounts.AccountsGateway;
import com.example.bankapp.transfer.integration.blocker.BlockerGateway;
import com.example.bankapp.transfer.integration.exchange.ExchangeGateway;
import com.example.bankapp.transfer.integration.exchange.dto.ConvertResponse;
import com.example.bankapp.transfer.model.CalculationResult;
import com.example.bankapp.transfer.model.Currency;
import com.example.bankapp.transfer.model.Transfer;
import com.example.bankapp.transfer.model.TransferStatus;
import com.example.bankapp.transfer.repository.TransferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@RequiredArgsConstructor
@Service
public class TransferService {
    private final TransferRepository repository;
    private final ExchangeGateway exchangeGateway;
    private final AccountsGateway accountsGateway;
    private final BlockerGateway blockerGateway;

    @Transactional
    public Transfer save(Transfer transfer) {
        return repository.save(transfer);
    }

    @Transactional
    public void saveCompletedAndNotify(Transfer transfer) {
        repository.save(transfer);
        // TODO: Реализовать оповещение через outbox
    }

    @Transactional(readOnly = true)
    public List<Transfer> getRetryRequired() {
        return repository.findAllByStatusEquals(TransferStatus.RETRY_REQUIRED);
    }

    @Retryable(includes = {ServiceUnavailableException.class})
    public CalculationResult calculateTargetAmount(Transfer transfer) {
        transfer.validateForCalculateTargetAmount();
        Currency fromCurrency = accountsGateway
                .getBankAccount(transfer.getFromAccountId())
                .currency();
        Currency toCurrency = accountsGateway
                .getBankAccount(transfer.getToAccountId())
                .currency();
        BigDecimal amount = transfer.getSourceAmount();

        if (fromCurrency == toCurrency) {
            return new CalculationResult(fromCurrency, toCurrency, null, null, amount);
        } else {
            ConvertResponse response = exchangeGateway.convert(fromCurrency, toCurrency, amount);
            return new CalculationResult(fromCurrency, toCurrency,
                    response.baseCurrencyRate(), response.toCurrencyRate(), response.converted());
        }
    }

    @Retryable(includes = {ServiceUnavailableException.class})
    public void withdrawSender(Transfer transfer) {
        transfer.validateForWithdraw();
        accountsGateway.withdraw(
                transfer.getFromAccountId(),
                transfer.getSourceAmount(),
                transfer.getIdempotencyKey()
        );
    }

    @Retryable(includes = {ServiceUnavailableException.class})
    public void depositReceiver(Transfer transfer) {
        transfer.validateForDeposit();
        accountsGateway.deposit(
                transfer.getToAccountId(),
                transfer.getTargetAmount(),
                transfer.getIdempotencyKey()
        );
    }

    @Retryable(includes = {ServiceUnavailableException.class})
    public void refundWithdraw(Transfer transfer) {
        transfer.validateForRefund();
        accountsGateway.deposit(
                transfer.getFromAccountId(),
                transfer.getSourceAmount(),
                transfer.getIdempotencyKey()
        );
    }

    @Retryable(includes = {ServiceUnavailableException.class})
    public boolean shouldBlock(Transfer transfer) {
        Long accountId = transfer.getFromAccountId();
        return blockerGateway.checkAccountOperation(accountId).block();
    }
}
