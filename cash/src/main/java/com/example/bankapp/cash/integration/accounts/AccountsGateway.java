package com.example.bankapp.cash.integration.accounts;

import com.example.bankapp.cash.exceptions.*;
import com.example.bankapp.cash.integration.accounts.dto.request.BankAccountDepositRequest;
import com.example.bankapp.cash.integration.accounts.dto.request.BankAccountWithdrawRequest;
import com.example.bankapp.cash.integration.accounts.dto.response.BankAccountResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class AccountsGateway {
    private final AccountsServiceClient client;

    public BankAccountResponse deposit(Long accountId, BigDecimal amount, UUID idempotencyKey) {
        var request = new BankAccountDepositRequest(amount);
        try {
            return client.deposit(accountId, request, idempotencyKey);
        } catch (RuntimeException e) {
            handleHttpError(e);
            throw e;
        }
    }

    public BankAccountResponse withdraw(Long accountId, BigDecimal amount, UUID idempotencyKey) {
        var request = new BankAccountWithdrawRequest(amount);
        try {
            return client.withdraw(accountId, request, idempotencyKey);
        } catch (RuntimeException e) {
            handleHttpError(e);
            throw e;
        }
    }

    private void handleHttpError(RuntimeException e) {
        log.error("Error executing request to the `Account service`: {}", e.getMessage());

        switch (e) {
            case HttpClientErrorException.NotFound ex ->
                    throw new ResourceNotFoundException(getErrorMessage(ex));
            case HttpClientErrorException.UnprocessableContent ex ->
                    throw new ServiceUnprocessableContent(getErrorMessage(ex));
            case HttpClientErrorException ex ->
                    throw new ServiceClientErrorException(getErrorMessage(ex));
            case HttpServerErrorException ex ->
                    throw new ServiceServerErrorException(getErrorMessage(ex));
            case ResourceAccessException ignored ->
                    throw new ServiceUnavailableException("Служба аккаунтов недоступна");
            default -> throw e;
        }
    }

    private String getErrorMessage(HttpStatusCodeException e) {
        ProblemDetail problemDetail = e.getResponseBodyAs(ProblemDetail.class);
        if (problemDetail == null || problemDetail.getDetail() == null) {
            return getDefaultDetail(e.getStatusCode());
        }

        return problemDetail.getDetail();
    }

    private String getDefaultDetail(HttpStatusCode statusCode) {
        HttpStatus status = HttpStatus.resolve(statusCode.value());
        return switch (status) {
            case NOT_FOUND -> "Запрашиваемый ресурс не найден в сервисе аккаунтов";
            case UNPROCESSABLE_CONTENT -> "Сервис аккаунтов отклонил операцию: нарушены условия её выполнения";
            case BAD_REQUEST -> "Некорректный запрос к сервису аккаунтов";
            case BAD_GATEWAY -> "Ошибка обработки запроса сервисом аккаунтов";
            case null -> "Неизвестный ответ от сервиса аккаунтов";
            default -> status.getReasonPhrase();
        };
    }
}
