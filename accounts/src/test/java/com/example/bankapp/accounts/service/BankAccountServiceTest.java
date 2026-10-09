package com.example.bankapp.accounts.service;

import com.example.bankapp.accounts.config.JdbcDialectTestConfiguration;
import com.example.bankapp.accounts.config.OAuth2TestConfiguration;
import com.example.bankapp.accounts.exception.EntityAlreadyExistsException;
import com.example.bankapp.accounts.exception.EntityNotFoundException;
import com.example.bankapp.accounts.exception.IdempotencyKeyException;
import com.example.bankapp.accounts.exception.InvalidAmountException;
import com.example.bankapp.accounts.exception.NotEnoughBalanceException;
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
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

@SpringBootTest
@Import({JdbcDialectTestConfiguration.class, OAuth2TestConfiguration.class})
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
        assertThat(found.getCreatedAt())
                .isCloseTo(rubAccount.getCreatedAt(), within(1, ChronoUnit.MICROS));
        assertThat(found.getUpdatedAt())
                .isCloseTo(rubAccount.getUpdatedAt(), within(1, ChronoUnit.MICROS));
        assertThat(found)
                .usingRecursiveComparison()
                .ignoringFields("createdAt", "updatedAt")
                .isEqualTo(rubAccount);
    }

    @Test
    void findById_nonExistingId_throwsException() {
        assertThatThrownBy(() -> bankAccountService.findById(1000L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void findByNumber_existingNumber_returnsBankAccount() {
        BankAccount rubAccount = bankAccountService.create(rubBankAccount);
        BankAccount found = bankAccountService.findByNumber(rubAccount.getAccountNumber());

        assertThat(found).isNotNull();
        assertThat(found.getCreatedAt())
                .isCloseTo(rubAccount.getCreatedAt(), within(1, ChronoUnit.MICROS));
        assertThat(found.getUpdatedAt())
                .isCloseTo(rubAccount.getUpdatedAt(), within(1, ChronoUnit.MICROS));
        assertThat(found)
                .usingRecursiveComparison()
                .ignoringFields("createdAt", "updatedAt")
                .isEqualTo(rubAccount);
    }

    @Test
    void findByNumber_nonExistingNumber_throwsException() {
        assertThatThrownBy(() -> bankAccountService.findByNumber("non_existing_number"))
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

    @Test
    void deposit_positiveAmount_returnsBankAccount() {
        BankAccount created = bankAccountService.create(rubBankAccount);
        BigDecimal amount = new BigDecimal("33.33");
        BigDecimal newBalance = created.getBalance()
                .add(amount)
                .setScale(2, RoundingMode.HALF_UP);

        BankAccount updated = bankAccountService.deposit(created.getId(), amount, UUID.randomUUID());

        assertThat(updated).isNotNull();
        assertThat(updated.getId()).isEqualTo(created.getId());
        assertThat(updated.getBalance()).isEqualTo(newBalance);
    }

    @Test
    void deposit_negativeAmount_throwsException() {
        BankAccount created = bankAccountService.create(rubBankAccount);
        BigDecimal amount = new BigDecimal("-33.33");

        assertThatThrownBy(() -> bankAccountService.deposit(created.getId(), amount, UUID.randomUUID()))
                .isInstanceOf(InvalidAmountException.class);
    }

    @Test
    void deposit_nullIdempotencyKey_throwsException() {
        BankAccount created = bankAccountService.create(rubBankAccount);
        BigDecimal amount = new BigDecimal("50.00");

        assertThatThrownBy(() -> bankAccountService.deposit(created.getId(), amount, null))
                .isInstanceOf(IdempotencyKeyException.class);
    }

    @Test
    void deposit_sameIdempotencyKey_executesOnlyOnce() {
        BankAccount created = bankAccountService.create(rubBankAccount);
        BigDecimal amount = new BigDecimal("50.00");
        UUID idempotencyKey = UUID.randomUUID();

        BankAccount firstDeposit = bankAccountService.deposit(created.getId(), amount, idempotencyKey);
        assertThat(firstDeposit.getBalance()).isEqualByComparingTo(new BigDecimal("50.00"));

        BankAccount secondDeposit = bankAccountService.deposit(created.getId(), amount, idempotencyKey);
        assertThat(secondDeposit.getBalance()).isEqualByComparingTo(new BigDecimal("50.00"));

        BankAccount accountInDb = bankAccountService.findById(created.getId());
        assertThat(accountInDb.getBalance()).isEqualByComparingTo(new BigDecimal("50.00"));
    }

    @Test
    void withdraw_amountLessThanBalance_returnsBankAccount() {
        BankAccount created = bankAccountService.create(rubBankAccount);
        created = bankAccountService.deposit(created.getId(), new BigDecimal("100.00"), UUID.randomUUID());
        BigDecimal amount = new BigDecimal("10.33");
        BigDecimal newBalance = created.getBalance()
                .subtract(amount)
                .setScale(2, RoundingMode.HALF_UP);

        assertThat(amount).isLessThan(created.getBalance());
        BankAccount updated = bankAccountService.withdraw(created.getId(), amount, UUID.randomUUID());

        assertThat(updated).isNotNull();
        assertThat(updated.getId()).isEqualTo(created.getId());
        assertThat(updated.getBalance()).isEqualTo(newBalance);
    }

    @Test
    void withdraw_amountGreaterThanBalance_throwsException() {
        BankAccount created = bankAccountService.create(rubBankAccount);
        BigDecimal amount = new BigDecimal("99999.99");

        assertThat(amount).isGreaterThan(created.getBalance());
        assertThatThrownBy(() -> bankAccountService.withdraw(created.getId(), amount, UUID.randomUUID()))
                .isInstanceOf(NotEnoughBalanceException.class);
    }

    @Test
    void withdraw_NegativeAmount_throwsException() {
        BankAccount created = bankAccountService.create(rubBankAccount);
        BigDecimal amount = new BigDecimal("-33.33");

        assertThatThrownBy(() -> bankAccountService.withdraw(created.getId(), amount, UUID.randomUUID()))
                .isInstanceOf(InvalidAmountException.class);
    }

    @Test
    void withdraw_nullIdempotencyKey_throwsException() {
        BankAccount created = bankAccountService.create(rubBankAccount);
        bankAccountService.deposit(created.getId(), new BigDecimal("100.00"), UUID.randomUUID());
        BigDecimal amount = new BigDecimal("50.00");

        assertThatThrownBy(() -> bankAccountService.withdraw(created.getId(), amount, null))
                .isInstanceOf(IdempotencyKeyException.class);
    }

    @Test
    void withdraw_sameIdempotencyKey_executesOnlyOnce() {
        BankAccount created = bankAccountService.create(rubBankAccount);
        bankAccountService.deposit(created.getId(), new BigDecimal("100.00"), UUID.randomUUID());
        BigDecimal amount = new BigDecimal("40.00");
        UUID idempotencyKey = UUID.randomUUID();

        BankAccount firstWithdraw = bankAccountService.withdraw(created.getId(), amount, idempotencyKey);
        assertThat(firstWithdraw.getBalance()).isEqualByComparingTo(new BigDecimal("60.00"));

        BankAccount secondWithdraw = bankAccountService.withdraw(created.getId(), amount, idempotencyKey);
        assertThat(secondWithdraw.getBalance()).isEqualByComparingTo(new BigDecimal("60.00"));

        BankAccount accountInDb = bankAccountService.findById(created.getId());
        assertThat(accountInDb.getBalance()).isEqualByComparingTo(new BigDecimal("60.00"));
    }
}
