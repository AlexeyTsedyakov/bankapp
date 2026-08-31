package com.example.bankapp.accounts.service;

import com.example.bankapp.accounts.config.JdbcDialectTestConfiguration;
import com.example.bankapp.accounts.exception.EntityAlreadyExistsException;
import com.example.bankapp.accounts.exception.EntityNotFoundException;
import com.example.bankapp.accounts.model.BankAccount;
import com.example.bankapp.accounts.model.Currency;
import com.example.bankapp.accounts.model.User;
import com.example.bankapp.accounts.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(JdbcDialectTestConfiguration.class)
@Transactional
@ActiveProfiles("test")
public class BankAccountServiceTest {
    @Autowired
    private BankAccountService bankAccountService;
    @Autowired
    private UserRepository userRepository;

    private BankAccount rubBankAccount;
    private BankAccount usdBankAccount;

    @BeforeEach
    public void setUp() {
        User testUser = User.builder()
                .id(null)
                .username("username")
                .password("password")
                .firstName("firstName")
                .lastName("lastName")
                .birthDate(LocalDate.of(2000, 12, 31))
                .email("email")
                .build();
        testUser = userRepository.save(testUser);

        rubBankAccount = BankAccount.builder()
                .user(AggregateReference.to(testUser.getId()))
                .currency(Currency.RUB)
                .build();
        usdBankAccount = BankAccount.builder()
                .user(AggregateReference.to(testUser.getId()))
                .currency(Currency.USD)
                .build();
    }

    @Test
    void create_bankAccountWithNonExistingUserCurrency_returnsBankAccount() {
        BankAccount rubAccount = bankAccountService.create(rubBankAccount);
        assertThat(rubAccount).isNotNull();
        assertThat(rubAccount.getId()).isNotNull();
        assertThat(rubAccount.getAccountNumber()).isNotNull();
        assertThat(rubAccount.getBalance()).isEqualByComparingTo(new BigDecimal("0.00"));

        BankAccount usdAccount = bankAccountService.create(usdBankAccount);
        assertThat(usdAccount).isNotNull();
        assertThat(usdAccount.getId()).isNotNull();
        assertThat(usdAccount.getAccountNumber()).isNotNull();
        assertThat(usdAccount.getAccountNumber()).isNotEqualTo(rubAccount.getAccountNumber());
        assertThat(usdAccount.getBalance()).isEqualByComparingTo(new BigDecimal("0.00"));
    }

    @Test
    void create_bankAccountWithExistingUserCurrency_throwsException() {
        bankAccountService.create(rubBankAccount);

        assertThatThrownBy(() -> bankAccountService.create(rubBankAccount))
                .isInstanceOf(EntityAlreadyExistsException.class);
    }

    @Test
    void findById_existingId_returnsBankAccount() {
        BankAccount rubAccount = bankAccountService.create(rubBankAccount);
        BankAccount found = bankAccountService.findById(rubAccount.getId());

        assertThat(found).isNotNull();
        assertThat(found)
                .usingRecursiveComparison()
                .isEqualTo(rubAccount);
    }

    @Test
    void findById_nonExistingId_throwsException() {
        assertThatThrownBy(() -> bankAccountService.findById(1000L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void findAllByUserId_existingId_returnsBankAccountList() {
        bankAccountService.create(rubBankAccount);
        bankAccountService.create(usdBankAccount);

        Long userId = rubBankAccount.getUser().getId();
        List<BankAccount> found = bankAccountService.findAllByUserId(userId);

        assertThat(found).isNotNull();
        assertThat(found).hasSize(2);
    }

    @Test
    void delete_existingId_deletesAccount() {
        BankAccount rubAccount = bankAccountService.create(rubBankAccount);
        bankAccountService.delete(rubAccount.getId());

        assertThatThrownBy(() -> bankAccountService.findById(rubAccount.getId()))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
