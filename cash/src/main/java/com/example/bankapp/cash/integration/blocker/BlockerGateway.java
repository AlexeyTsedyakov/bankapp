package com.example.bankapp.cash.integration.blocker;

import com.example.bankapp.cash.exceptions.*;
import com.example.bankapp.cash.integration.blocker.dto.BlockerRequest;
import com.example.bankapp.cash.integration.blocker.dto.BlockerResponse;
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

@Slf4j
@RequiredArgsConstructor
@Component
public class BlockerGateway {
    private final BlockerServiceClient client;

    public BlockerResponse checkAccountOperation(long accountId) {
        var request = new BlockerRequest(accountId);
        try {
            return client.checkAccountOperation(request);
        } catch (RuntimeException e) {
            handleHttpError(e);
            throw e;
        }
    }

    private void handleHttpError(RuntimeException e) {
        log.error("Error executing request to the `Account service`: {}", e.getMessage());

        switch (e) {
            case HttpClientErrorException ex ->
                    throw new ServiceClientErrorException(getErrorMessage(ex));
            case HttpServerErrorException ex ->
                    throw new ServiceServerErrorException(getErrorMessage(ex));
            case ResourceAccessException ignored ->
                    throw new ServiceUnavailableException("Служба блокировки подозрительных операций недоступна");
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
            case BAD_REQUEST -> "Некорректный запрос к сервису блокировки подозрительных операций";
            case BAD_GATEWAY -> "Ошибка обработки запроса сервисом блокировки подозрительных операций";
            case null -> "Неизвестный ответ от сервиса блокировки подозрительных операций";
            default -> status.getReasonPhrase();
        };
    }
}
