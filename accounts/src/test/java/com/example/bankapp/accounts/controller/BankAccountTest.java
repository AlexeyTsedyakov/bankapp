package com.example.bankapp.accounts.controller;

import com.example.bankapp.accounts.config.JdbcDialectTestConfiguration;
import com.example.bankapp.accounts.controller.dto.request.BankAccountCreateRequest;
import com.example.bankapp.accounts.controller.dto.request.UserRegisterRequest;
import com.example.bankapp.accounts.controller.dto.response.BankAccountResponse;
import com.example.bankapp.accounts.controller.dto.response.UserResponse;
import com.example.bankapp.accounts.model.Currency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.io.UnsupportedEncodingException;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@Import(JdbcDialectTestConfiguration.class)
@Transactional
@ActiveProfiles("test")
public class BankAccountTest {
    @Autowired
    private MockMvcTester mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    private BankAccountCreateRequest rubCreateRequestDto;

    @BeforeEach
    void setUp() throws UnsupportedEncodingException {
        UserResponse userResponse = registerUser();

        rubCreateRequestDto = BankAccountCreateRequest.builder()
                .userId(userResponse.id())
                .currency(Currency.RUB)
                .build();
    }

    @Test
    @WithMockUser
    void createBankAccount_withNonExistingUserCurrency_returns200() {
        var request = mockMvc.post()
                .uri("/bank-accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(rubCreateRequestDto));

        assertThat(request)
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.id", id ->
                        assertThat(id).isNotNull())
                .hasPathSatisfying("$.accountNumber", accountNumber ->
                        assertThat(accountNumber).asString().isNotBlank());
    }

    @Test
    @WithMockUser
    void createBankAccount_withExistingUserCurrency_returns409() {
        var request = mockMvc.post()
                .uri("/bank-accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(rubCreateRequestDto));

        assertThat(request).hasStatusOk();
        assertThat(request).hasStatus(HttpStatus.CONFLICT);
    }

    @Test
    @WithMockUser
    void deleteBankAccount_withExistingAccount_returns204() throws UnsupportedEncodingException {
        BankAccountResponse accountDto = createBankAccount();
        var request = mockMvc.delete()
                .uri("/bank-accounts/{id}", accountDto.id());

        assertThat(request).hasStatus(HttpStatus.NO_CONTENT);
    }

    private UserResponse registerUser() throws UnsupportedEncodingException {
        UserRegisterRequest registerRequestDto = UserRegisterRequest.builder()
                .username("test_user")
                .password("password")
                .firstName("Alice")
                .lastName("Wonder")
                .birthDate(LocalDate.of(1995, 5, 20))
                .email("alice@example.com")
                .build();

        var registerResponse = mockMvc.post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequestDto))
                .exchange()
                .getMvcResult()
                .getResponse();

        return objectMapper.readValue(
                registerResponse.getContentAsString(), UserResponse.class);
    }

    private BankAccountResponse createBankAccount() throws UnsupportedEncodingException {
        var createResponse = mockMvc.post()
                .uri("/bank-accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(rubCreateRequestDto))
                .exchange()
                .getResponse();

        return objectMapper.readValue(
                createResponse.getContentAsString(), BankAccountResponse.class);
    }
}
