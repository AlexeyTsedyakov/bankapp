package com.example.bankapp.transfer.integration.exchange;

import com.example.bankapp.transfer.exceptions.ResourceNotFoundException;
import com.example.bankapp.transfer.exceptions.ServiceClientErrorException;
import com.example.bankapp.transfer.exceptions.ServiceServerErrorException;
import com.example.bankapp.transfer.exceptions.ServiceUnavailableException;
import com.example.bankapp.transfer.integration.exchange.dto.ConvertRequest;
import com.example.bankapp.transfer.integration.exchange.dto.ConvertResponse;
import com.example.bankapp.transfer.integration.exchange.dto.ExchangeRate;
import com.example.bankapp.transfer.model.Currency;
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

@Slf4j
@RequiredArgsConstructor
@Component
public class ExchangeGateway {
    private final ExchangeServiceClient exchangeServiceClient;

    public ExchangeRate getExchangeRate(Currency currency) {
        try {
            return exchangeServiceClient.getExchangeRate(currency);
        } catch (RuntimeException e) {
            handleHttpError(e);
            throw e;
        }
    }

    public ConvertResponse convert(Currency from, Currency to, BigDecimal amount) {
        var request = new ConvertRequest(from, to, amount);

        try {
            return exchangeServiceClient.convert(request);
        } catch (RuntimeException e) {
            handleHttpError(e);
            throw e;
        }
    }

    private void handleHttpError(RuntimeException e) {
        log.error("Error executing request to the `Exchange service`: {}", e.getMessage());

        switch (e) {
            case HttpClientErrorException.NotFound ex ->
                    throw new ResourceNotFoundException(getErrorMessage(ex));
            case HttpClientErrorException ex ->
                    throw new ServiceClientErrorException(getErrorMessage(ex));
            case HttpServerErrorException ex ->
                    throw new ServiceServerErrorException(getErrorMessage(ex));
            case ResourceAccessException ignored ->
                    throw new ServiceUnavailableException("Служба конвертации валют недоступна");
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
            case NOT_FOUND -> "Запрашиваемый ресурс не найден в сервисе конвертации валют";
            case BAD_REQUEST -> "Некорректный запрос к сервису конвертации валют";
            case BAD_GATEWAY -> "Ошибка обработки запроса сервисом конвертации валют";
            case null -> "Неизвестный ответ от сервиса конвертации валют";
            default -> status.getReasonPhrase();
        };
    }
}
