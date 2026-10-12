package org.example.bankapp.front.integration.cash;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.bankapp.front.common.exceptions.IntegrationException;
import org.example.bankapp.front.integration.cash.dto.CashDepositRequest;
import org.example.bankapp.front.integration.cash.dto.CashWithdrawRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class CashGateway {
    private final CashServiceClient client;

    public void deposit(CashDepositRequest depositRequest, UUID idempotencyKey) {
        try {
            client.deposit(depositRequest, idempotencyKey);
        } catch (RuntimeException e) {
            handleHttpError(e);
            throw e;
        }
    }

    public void withdraw(CashWithdrawRequest withdrawRequest, UUID idempotencyKey) {
        try {
            client.withdraw(withdrawRequest, idempotencyKey);
        } catch (HttpClientErrorException.UnprocessableContent e) {
            throw new IntegrationException("Не достаточно средств на балансе!");
        } catch (RuntimeException e) {
            handleHttpError(e);
            throw e;
        }
    }

    private void handleHttpError(RuntimeException e) {
        log.error("Error executing request to the `Cash service`: {}", e.getMessage());

        switch (e) {
            case HttpClientErrorException.Forbidden _ -> throw new IntegrationException("Операция заблокирована");
            case HttpStatusCodeException _ ->
                    throw new IntegrationException("Ошибка выполнения запроса к сервису наличных");
            case ResourceAccessException _ -> throw new IntegrationException("Служба наличных не доступна");
            default -> throw e;
        }
    }
}
