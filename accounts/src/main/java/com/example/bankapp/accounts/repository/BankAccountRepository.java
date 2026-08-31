package com.example.bankapp.accounts.repository;

import com.example.bankapp.accounts.model.BankAccount;
import com.example.bankapp.accounts.model.Currency;
import com.example.bankapp.accounts.model.User;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BankAccountRepository extends ListCrudRepository<BankAccount, Long> {
    List<BankAccount> findAllByUser(AggregateReference<User, Long> user);

    @Query("SELECT nextval('bank_account_number_seq')")
    Long getNextAccountNumber();

    boolean existsByUserAndCurrency(AggregateReference<User, Long> user, Currency currency);
}