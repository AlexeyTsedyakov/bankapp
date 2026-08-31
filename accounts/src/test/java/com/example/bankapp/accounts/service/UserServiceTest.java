package com.example.bankapp.accounts.service;

import com.example.bankapp.accounts.config.JdbcDialectTestConfiguration;
import com.example.bankapp.accounts.exception.EntityAlreadyExistsException;
import com.example.bankapp.accounts.exception.EntityNotFoundException;
import com.example.bankapp.accounts.exception.UserHasBankAccountsException;
import com.example.bankapp.accounts.model.BankAccount;
import com.example.bankapp.accounts.model.Currency;
import com.example.bankapp.accounts.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Import(JdbcDialectTestConfiguration.class)
@Transactional
@ActiveProfiles("test")
public class UserServiceTest {
    @Autowired
    private UserService userService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private BankAccountService bankAccountService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(null)
                .username("username")
                .password("password")
                .firstName("firstName")
                .lastName("lastName")
                .birthDate(LocalDate.of(2000, 12, 31))
                .email("email")
                .build();
    }

    @Test
    void save_userWithNonExistingUsername_returnsUser() {
        User created = userService.create(testUser);

        assertThat(created).isNotNull();
        assertThat(created.getId()).isNotNull();
        assertThat(created.getCreatedAt()).isNotNull();
        assertThat(created.getUpdatedAt()).isNotNull();
        assertThat(passwordEncoder.matches(testUser.getPassword(), created.getPassword()));
        assertThat(created)
                .usingRecursiveComparison()
                .ignoringFields("id", "password", "createdAt", "updatedAt")
                .isEqualTo(testUser);
    }

    @Test
    void save_userWithExistingUsername_shouldThrowException_throwsException() {
        userService.create(testUser);

        assertThatThrownBy(() -> userService.create(testUser))
                .isInstanceOf(EntityAlreadyExistsException.class);
    }

    @Test
    void findById_existingUser_returnsUser() {
        User created = userService.create(testUser);
        User found = userService.findById(created.getId());

        assertThat(found).isNotNull();
        assertThat(found.getCreatedAt())  // Java - 9 знаков наносекунд, БД - 6
                .isCloseTo(created.getCreatedAt(), within(1, ChronoUnit.MICROS));
        assertThat(found.getUpdatedAt())
                .isCloseTo(created.getUpdatedAt(), within(1, ChronoUnit.MICROS));
        assertThat(found)
                .usingRecursiveComparison()
                .ignoringFields("createdAt", "updatedAt")
                .isEqualTo(created);
    }

    @Test
    void findById_nonExistingUser_throwsException() {
        assertThatThrownBy(() -> userService.findById(1000L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void findByUsername_existingUser_returnsUser() {
        User created = userService.create(testUser);
        User found = userService.findByUsername(testUser.getUsername());

        assertThat(found).isNotNull();
        assertThat(found.getCreatedAt())  // Java - 9 знаков наносекунд, БД - 6
                .isCloseTo(created.getCreatedAt(), within(1, ChronoUnit.MICROS));
        assertThat(found.getUpdatedAt())
                .isCloseTo(created.getUpdatedAt(), within(1, ChronoUnit.MICROS));
        assertThat(found)
                .usingRecursiveComparison()
                .ignoringFields("createdAt", "updatedAt")
                .isEqualTo(created);
    }

    @Test
    void findByUsername_nonExistingUser_throwsException() {
        assertThatThrownBy(() -> userService.findByUsername("nonExistent"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void update_existingUser_returnsUser() {
        User created = userService.create(testUser);
        created.setFirstName("updated");

        User updated = userService.update(created);

        assertThat(updated).isNotNull();
        assertThat(updated.getFirstName()).isEqualTo(created.getFirstName());
        assertThat(updated.getUpdatedAt()).isNotEqualTo(created.getCreatedAt());
        assertThat(updated.getCreatedAt())  // Java - 9 знаков наносекунд, БД - 6
                .isCloseTo(created.getCreatedAt(), within(1, ChronoUnit.MICROS));
        assertThat(updated)
                .usingRecursiveComparison()
                .ignoringFields("firstName", "updatedAt", "createdAt")
                .isEqualTo(created);
    }

    @Test
    void update_nonExistingUser_throwsException() {
        testUser.setId(1000L);
        assertThatThrownBy(() -> userService.update(testUser))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void updatePassword_userIdWithNewPassword_changesPassword() {
        String newPassword = "newPassword";
        User created = userService.create(testUser);

        userService.updatePassword(created.getId(), newPassword);
        User found = userService.findById(created.getId());

        assertThat(found).isNotNull();
        assertThat(passwordEncoder.matches(newPassword ,found.getPassword()));
    }

    @Test
    void delete_userWithoutBankAccounts_deletesUser() {
        User created = userService.create(testUser);
        userService.delete(created.getId());

        assertThatThrownBy(() -> userService.findById(created.getId()))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void delete_userWithBankAccounts_thrownException() {
        User created = userService.create(testUser);
        BankAccount bankAccount = BankAccount.builder()
                .user(AggregateReference.to(created.getId()))
                .currency(Currency.RUB)
                .build();
        bankAccountService.create(bankAccount);

        assertThatThrownBy(() -> userService.delete(created.getId()))
                .isInstanceOf(UserHasBankAccountsException.class);
    }
}
