package com.example.bankapp.accounts.repository;

import com.example.bankapp.accounts.model.BankAccount;
import com.example.bankapp.accounts.model.Currency;
import com.example.bankapp.accounts.model.User;
import org.springframework.data.domain.Sort;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BankAccountRepository extends ListCrudRepository<BankAccount, Long> {
    List<BankAccount> findAllByUser(AggregateReference<User, Long> user, Sort sort);

    @Query("SELECT nextval('bank_account_number_seq')")
    Long getNextAccountNumber();

    boolean existsByUserAndCurrency(AggregateReference<User, Long> user, Currency currency);

    Optional<BankAccount> findByAccountNumber(String accountNumber);

    @Query("SELECT * FROM bank_accounts WHERE id = :id FOR UPDATE")
    Optional<BankAccount> findByIdForUpdate(Long id);
}