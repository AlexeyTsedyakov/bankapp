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

@Slf4j
@RequiredArgsConstructor
@Component
public class CashGateway {
    private final CashServiceClient client;

    public void deposit(CashDepositRequest depositRequest) {
        try {
            client.deposit(depositRequest);
        } catch (RuntimeException e) {
            handleHttpError(e);
            throw e;
        }
    }

    public void withdraw(CashWithdrawRequest withdrawRequest) {
        try {
            client.withdraw(withdrawRequest);
        } catch (HttpClientErrorException.UnprocessableContent e) {
            throw new IntegrationException("Не достаточно средств на балансе!");
        } catch (RuntimeException e) {
            handleHttpError(e);
            throw e;
        }
    }

    private void handleHttpError(Exception e) {
        log.error("Error executing request to the `Cash service`: {}", e.getMessage());

        if (e instanceof HttpStatusCodeException) {
            throw new IntegrationException("Ошибка выполнения запроса к сервису наличных");
        } else if (e instanceof ResourceAccessException) {
            throw new IntegrationException("Служба наличных не доступна");
        }
    }
}
