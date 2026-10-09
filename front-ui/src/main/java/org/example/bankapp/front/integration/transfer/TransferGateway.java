package org.example.bankapp.front.integration.transfer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.bankapp.front.common.exceptions.IntegrationException;
import org.example.bankapp.front.integration.transfer.dto.TransferRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class TransferGateway {
    private final TransferServiceClient client;

    public void transfer(TransferRequest request, UUID idempotencyKey) {
        try {
            client.transfer(request, idempotencyKey);
        } catch (RuntimeException e) {
            handleHttpError(e);
            throw e;
        }
    }

    private void handleHttpError(RuntimeException e) {
        log.error("Error executing request to the `Transfer service`: {}", e.getMessage());

        if (e instanceof HttpStatusCodeException) {
            throw new IntegrationException("Ошибка выполнения запроса к сервису обмена");
        } else if (e instanceof ResourceAccessException) {
            throw new IntegrationException("Служба обмена не доступна");
        }
    }
}

