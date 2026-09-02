package com.example.bankapp.accounts.controller;

import com.example.bankapp.accounts.config.JdbcDialectTestConfiguration;
import com.example.bankapp.accounts.controller.dto.request.BankAccountCreateRequest;
import com.example.bankapp.accounts.controller.dto.request.PasswordUpdateRequest;
import com.example.bankapp.accounts.controller.dto.request.UserRegisterRequest;
import com.example.bankapp.accounts.controller.dto.request.UserUpdateRequest;
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
public class UserControllerTest {
    @Autowired
    private MockMvcTester mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    private UserRegisterRequest registerRequestDto;

    @BeforeEach
    void setUp() {
        registerRequestDto = UserRegisterRequest.builder()
                .username("test_user")
                .password("password")
                .firstName("Alice")
                .lastName("Wonder")
                .birthDate(LocalDate.of(1995, 5, 20))
                .email("alice@example.com")
                .build();
    }

    @Test
    @WithMockUser(roles = "accounts-access")
    void registerUser_NonExistingUsername_returns200() {
        var request = mockMvc.post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequestDto));

        assertThat(request)
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.id", id ->
                        assertThat(id).isNotNull())
                .hasPathSatisfying("$.username", username ->
                        assertThat(username).isEqualTo(registerRequestDto.username()));
    }

    @Test
    @WithMockUser(roles = "accounts-access")
    void registerUser_ExistingUsername_returns409() {
        var request = mockMvc.post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequestDto));

        assertThat(request).hasStatusOk();
        assertThat(request).hasStatus(HttpStatus.CONFLICT);
    }

    @Test
    @WithMockUser(roles = "accounts-access")
    void getUserWithBankAccounts_ExistingId_returns200() throws UnsupportedEncodingException {
        UserResponse responseDto = registerUser();
        var getUserRequest = mockMvc.get()
                .uri("/users/{id}", responseDto.id());

        assertThat(getUserRequest)
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.user.id", id ->
                        assertThat(id).convertTo(Long.class).isEqualTo(responseDto.id()));
    }

    @Test
    @WithMockUser(roles = "accounts-access")
    void getUserBankAccounts_NonExistingId_returns404() {
        var request = mockMvc.get()
                .uri("/users/{id}", 999L);
        assertThat(request)
                .hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    @WithMockUser(roles = "accounts-access")
    void getUserCredentials_ExistingUsername_returns200() {
        mockMvc.post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequestDto))
                .exchange();

        var request = mockMvc.get()
                .uri("/users/{username}/credentials", registerRequestDto.username());
        assertThat(request)
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.username", username ->
                        assertThat(username).isEqualTo(registerRequestDto.username()))
                .hasPathSatisfying("$.password", password ->
                        assertThat(password).isNotNull());
    }

    @Test
    @WithMockUser(roles = "accounts-access")
    void getUserCredentials_NonExistingUsername_returns404() {
        var request = mockMvc.get()
                .uri("/users/{username}/credentials", "NonExistentUsername");
        assertThat(request)
                .hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    @WithMockUser(roles = "accounts-access")
    void updateUser_ExistingUser_returns200() throws UnsupportedEncodingException {
        UserResponse responseDto = registerUser();
        UserUpdateRequest updateRequestDto = UserUpdateRequest.builder()
                .firstName("updated")
                .lastName(responseDto.lastName())
                .birthDate(responseDto.birthDate())
                .email(responseDto.email())
                .build();
        var updateRequest = mockMvc.put()
                .uri("/users/{id}", responseDto.id())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequestDto));

        assertThat(updateRequest)
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.firstName", firstName ->
                        assertThat(firstName).isEqualTo(updateRequestDto.firstName()));
    }

    @Test
    @WithMockUser(roles = "accounts-access")
    void updateUser_NonExistingUser_returns404() {
        UserUpdateRequest updateRequestDto = UserUpdateRequest.builder()
                .firstName("firstName")
                .lastName("lastName")
                .birthDate(LocalDate.of(2000, 1, 1))
                .email("email@test.com")
                .build();
        var updateRequest = mockMvc.put()
                .uri("/users/{id}", 999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequestDto));

        assertThat(updateRequest)
                .hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    @WithMockUser(roles = "accounts-access")
    void updateUserPassword_userIdWithNewPassword_returns204() throws UnsupportedEncodingException {
        UserResponse userDto = registerUser();
        PasswordUpdateRequest passwordUpdateRequest = new PasswordUpdateRequest("newPassword");
        var request = mockMvc.patch()
                .uri("/users/{id}/password", userDto.id())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(passwordUpdateRequest));

        assertThat(request)
                .hasStatus(HttpStatus.NO_CONTENT);
    }

    @Test
    @WithMockUser(roles = "accounts-access")
    void deleteUser_userWithoutBankAccounts_returns204() throws UnsupportedEncodingException {
        UserResponse userDto = registerUser();
        var request = mockMvc.delete()
                .uri("/users/{id}", userDto.id());

        assertThat(request)
                .hasStatus(HttpStatus.NO_CONTENT);
    }

    @Test
    @WithMockUser(roles = "accounts-access")
    void deleteUser_userWithBankAccounts_returns409() throws UnsupportedEncodingException {
        UserResponse userDto = registerUser();
        createBankAccount(userDto.id());
        var request = mockMvc.delete()
                .uri("/users/{id}", userDto.id());

        assertThat(request)
                .hasStatus(HttpStatus.CONFLICT);
    }

    private UserResponse registerUser() throws UnsupportedEncodingException {
        var registerResponse = mockMvc.post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequestDto))
                .exchange()
                .getResponse();

        return objectMapper.readValue(
                registerResponse.getContentAsString(), UserResponse.class);
    }

    private void createBankAccount(Long userId) throws UnsupportedEncodingException {
        BankAccountCreateRequest requestDto = BankAccountCreateRequest.builder()
                .userId(userId)
                .currency(Currency.RUB)
                .build();
        var createResponse = mockMvc.post()
                .uri("/bank-accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
                .exchange()
                .getResponse();

        objectMapper.readValue(
                createResponse.getContentAsString(), BankAccountResponse.class);
    }
}
