package com.example.bankapp.accounts.controller;

import com.example.bankapp.accounts.config.JdbcDialectTestConfiguration;
import com.example.bankapp.accounts.config.OAuth2TestConfiguration;
import com.example.bankapp.accounts.controller.dto.request.BankAccountCreateRequest;
import com.example.bankapp.accounts.controller.dto.request.BankAccountDepositRequest;
import com.example.bankapp.accounts.controller.dto.request.BankAccountWithdrawRequest;
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
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.BIG_DECIMAL;

@SpringBootTest
@AutoConfigureMockMvc
@Import({JdbcDialectTestConfiguration.class, OAuth2TestConfiguration.class})
@Transactional
@ActiveProfiles("test")
public class BankAccountControllerTest {
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
    @WithMockUser(roles = "accounts-access")
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
    @WithMockUser(roles = "accounts-access")
    void createBankAccount_withExistingUserCurrency_returns409() {
        var request = mockMvc.post()
                .uri("/bank-accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(rubCreateRequestDto));

        assertThat(request).hasStatusOk();
        assertThat(request).hasStatus(HttpStatus.CONFLICT);
    }

    @Test
    @WithMockUser(roles = "accounts-access")
    void deleteBankAccount_withExistingAccount_returns204() throws UnsupportedEncodingException {
        BankAccountResponse accountDto = createBankAccount();
        var request = mockMvc.delete()
                .uri("/bank-accounts/{id}", accountDto.id());

        assertThat(request).hasStatus(HttpStatus.NO_CONTENT);
    }

    @Test
    @WithMockUser(roles = "accounts-access")
    void deposit_withExistingAccountAndValidAmount_returns200() throws UnsupportedEncodingException {
        BankAccountResponse accountDto = createBankAccount();
        var debitRequest = new BankAccountDepositRequest(new BigDecimal("100.50"));

        var request = mockMvc.post()
                .uri("/bank-accounts/{id}/deposit", accountDto.id())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(debitRequest));

        assertThat(request)
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.id", id ->
                        assertThat(id).convertTo(Long.class).isEqualTo(accountDto.id()))
                .hasPathSatisfying("$.balance", balance ->
                        assertThat(balance).convertTo(BIG_DECIMAL).isEqualByComparingTo(new BigDecimal("100.50")));
    }

    @Test
    @WithMockUser(roles = "accounts-access")
    void deposit_withNonExistingAccount_returns404() {
        var debitRequest = new BankAccountDepositRequest(new BigDecimal("100.50"));

        var request = mockMvc.post()
                .uri("/bank-accounts/{id}/deposit", 999999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(debitRequest));

        assertThat(request).hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    @WithMockUser(roles = "accounts-access")
    void deposit_withNegativeAmount_returns400() throws UnsupportedEncodingException {
        BankAccountResponse accountDto = createBankAccount();
        var debitRequest = new BankAccountDepositRequest(new BigDecimal("-10.00"));

        var request = mockMvc.post()
                .uri("/bank-accounts/{id}/deposit", accountDto.id())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(debitRequest));

        assertThat(request).hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    @WithMockUser(roles = "accounts-access")
    void deposit_withZeroAmount_returns400() throws UnsupportedEncodingException {
        BankAccountResponse accountDto = createBankAccount();
        var debitRequest = new BankAccountDepositRequest(BigDecimal.ZERO);

        var request = mockMvc.post()
                .uri("/bank-accounts/{id}/deposit", accountDto.id())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(debitRequest));

        assertThat(request).hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    @WithMockUser(roles = "accounts-access")
    void withdraw_withSufficientBalance_returns200() throws UnsupportedEncodingException {
        BankAccountResponse accountDto = createBankAccount();
        var debitRequest = new BankAccountDepositRequest(new BigDecimal("100.00"));
        mockMvc.post()
                .uri("/bank-accounts/{id}/deposit", accountDto.id())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(debitRequest))
                .exchange();

        var withdrawRequest = new BankAccountWithdrawRequest(new BigDecimal("40.00"));
        var request = mockMvc.post()
                .uri("/bank-accounts/{id}/withdraw", accountDto.id())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(withdrawRequest));

        assertThat(request)
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.id", id ->
                        assertThat(id).convertTo(Long.class).isEqualTo(accountDto.id()))
                .hasPathSatisfying("$.balance", balance ->
                        assertThat(balance).convertTo(BIG_DECIMAL).isEqualByComparingTo(new BigDecimal("60.00")));
    }

    @Test
    @WithMockUser(roles = "accounts-access")
    void withdraw_withInsufficientBalance_returns422() throws UnsupportedEncodingException {
        BankAccountResponse accountDto = createBankAccount();
        var withdrawRequest = new BankAccountWithdrawRequest(new BigDecimal("50.00"));

        var request = mockMvc.post()
                .uri("/bank-accounts/{id}/withdraw", accountDto.id())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(withdrawRequest));

        assertThat(request).hasStatus(HttpStatus.UNPROCESSABLE_CONTENT);
    }

    @Test
    @WithMockUser(roles = "accounts-access")
    void withdraw_withNonExistingAccount_returns404() {
        var withdrawRequest = new BankAccountWithdrawRequest(new BigDecimal("40.00"));

        var request = mockMvc.post()
                .uri("/bank-accounts/{id}/withdraw", 999999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(withdrawRequest));

        assertThat(request).hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    @WithMockUser(roles = "accounts-access")
    void withdraw_withNegativeAmount_returns400() throws UnsupportedEncodingException {
        BankAccountResponse accountDto = createBankAccount();
        var withdrawRequest = new BankAccountWithdrawRequest(new BigDecimal("-10.00"));

        var request = mockMvc.post()
                .uri("/bank-accounts/{id}/withdraw", accountDto.id())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(withdrawRequest));

        assertThat(request).hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    @WithMockUser(roles = "accounts-access")
    void withdraw_withZeroAmount_returns400() throws UnsupportedEncodingException {
        BankAccountResponse accountDto = createBankAccount();
        var withdrawRequest = new BankAccountWithdrawRequest(BigDecimal.ZERO);

        var request = mockMvc.post()
                .uri("/bank-accounts/{id}/withdraw", accountDto.id())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(withdrawRequest));

        assertThat(request).hasStatus(HttpStatus.BAD_REQUEST);
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
