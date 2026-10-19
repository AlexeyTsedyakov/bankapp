package com.example.bankapp.transfer.service;

import com.example.bankapp.transfer.exceptions.ServiceUnavailableException;
import com.example.bankapp.transfer.integration.accounts.AccountsGateway;
import com.example.bankapp.transfer.integration.accounts.dto.response.BankAccountResponse;
import com.example.bankapp.transfer.integration.blocker.BlockerGateway;
import com.example.bankapp.transfer.integration.exchange.ExchangeGateway;
import com.example.bankapp.transfer.integration.exchange.dto.ConvertResponse;
import com.example.bankapp.transfer.model.CalculationResult;
import com.example.bankapp.transfer.model.Currency;
import com.example.bankapp.transfer.model.Transfer;
import com.example.bankapp.transfer.model.TransferStatus;
import com.example.bankapp.transfer.repository.TransferRepository;
import com.example.bankapp.transfer.service.event.TransferCompleted;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
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
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Transfer save(Transfer transfer) {
        return repository.save(transfer);
    }

    @Transactional
    public void saveCompletedAndNotify(Transfer transfer) {
        Transfer saved = repository.save(transfer);
        eventPublisher.publishEvent(new TransferCompleted(saved));
    }

    @Transactional(readOnly = true)
    public List<Transfer> getRetryRequired() {
        return repository.findAllByStatusEquals(TransferStatus.RETRY_REQUIRED);
    }

    @Retryable(includes = {ServiceUnavailableException.class})
    public boolean shouldBlock(Transfer transfer) {
        Long accountId = transfer.getFrom().accountId();
        return blockerGateway.checkAccountOperation(accountId).block();
    }

    @Retryable(includes = {ServiceUnavailableException.class})
    public Transfer.AccountSnapshot getAccountSnapshot(Long id) {
        BankAccountResponse response = accountsGateway.getBankAccount(id);

        return Transfer.AccountSnapshot.builder()
                .accountId(response.id())
                .userId(response.userId())
                .accountNumber(response.accountNumber())
                .currency(response.currency())
                .build();
    }

    @Retryable(includes = {ServiceUnavailableException.class})
    public CalculationResult calculateTargetAmount(Transfer transfer) {
        transfer.validateForCalculateTargetAmount();
        Currency fromCurrency = transfer.getFrom().currency();
        Currency toCurrency = transfer.getTo().currency();
        BigDecimal amount = transfer.getSourceAmount();

        if (fromCurrency == toCurrency) {
            return new CalculationResult(null, null, amount);
        } else {
            ConvertResponse response = exchangeGateway.convert(fromCurrency, toCurrency, amount);
            return new CalculationResult(
                    response.baseCurrencyRate(), response.toCurrencyRate(), response.converted());
        }
    }

    @Retryable(includes = {ServiceUnavailableException.class})
    public void withdrawSender(Transfer transfer) {
        transfer.validateForWithdraw();
        accountsGateway.withdraw(
                transfer.getFrom().accountId(),
                transfer.getSourceAmount(),
                transfer.getIdempotencyKey()
        );
    }

    @Retryable(includes = {ServiceUnavailableException.class})
    public void depositReceiver(Transfer transfer) {
        transfer.validateForDeposit();
        accountsGateway.deposit(
                transfer.getTo().accountId(),
                transfer.getTargetAmount(),
                transfer.getIdempotencyKey()
        );
    }

    @Retryable(includes = {ServiceUnavailableException.class})
    public void refundWithdraw(Transfer transfer) {
        transfer.validateForRefund();
        accountsGateway.deposit(
                transfer.getFrom().accountId(),
                transfer.getSourceAmount(),
                transfer.getIdempotencyKey()
        );
    }
}
