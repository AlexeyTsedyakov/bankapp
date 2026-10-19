package com.example.bankapp.transfer.integration.notifications;

import com.example.bankapp.transfer.exceptions.ServiceClientErrorException;
import com.example.bankapp.transfer.exceptions.ServiceServerErrorException;
import com.example.bankapp.transfer.exceptions.ServiceUnavailableException;
import com.example.bankapp.transfer.integration.notifications.dto.NotifyRequest;
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

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class NotificationsGateway {
    private final NotificationsServiceClient client;

    public void notify(String email, String text, UUID idempotencyKey) {
        var request = new NotifyRequest(idempotencyKey, email, text);
        try {
            client.notify(request);
        } catch (RuntimeException e) {
            handleHttpError(e);
            throw e;
        }
    }

    private void handleHttpError(RuntimeException e) {
        log.error("Error executing request to the `Notifications service`: {}", e.getMessage());

        switch (e) {
            case HttpClientErrorException ex ->
                    throw new ServiceClientErrorException(getErrorMessage(ex));
            case HttpServerErrorException ex ->
                    throw new ServiceServerErrorException(getErrorMessage(ex));
            case ResourceAccessException ignored ->
                    throw new ServiceUnavailableException("Служба уведомлений недоступна");
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
            case BAD_REQUEST -> "Некорректный запрос к сервису уведомлений";
            case BAD_GATEWAY -> "Ошибка обработки запроса сервисом уведомлений";
            case null -> "Неизвестный ответ от сервиса уведомлений";
            default -> status.getReasonPhrase();
        };
    }
}
