package org.example.bankapp.front.auth;

import lombok.RequiredArgsConstructor;
import org.example.bankapp.front.integration.accounts.AccountsServiceClient;
import org.example.bankapp.front.integration.accounts.dto.response.UserCredentialsResponse;
import org.example.bankapp.front.integration.accounts.exceptions.AccountsServiceException;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.util.List;

@RequiredArgsConstructor
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private final AccountsServiceClient accountsServiceClient;

    @Override
    @NonNull
    public UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        UserCredentialsResponse credentials;
        try {
            credentials = accountsServiceClient.getUserCredentials(username);
        } catch (HttpClientErrorException.NotFound e) {
            throw new UsernameNotFoundException("В сервисе аккаунтов отсутствует пользователь, логин: " + username , e);
        } catch (ResourceAccessException e) {
            throw new AccountsServiceException("Служба аккаунтов не доступна", e);
        } catch (Exception e) {
            throw new AccountsServiceException("Ошибка выполнения запроса к сервису аккаунтов", e);
        }

        return UserDetailsImpl.builder()
                .id(credentials.id())
                .username(credentials.username())
                .password(credentials.password())
                .authorities(List.of())
                .build();
    }
}
