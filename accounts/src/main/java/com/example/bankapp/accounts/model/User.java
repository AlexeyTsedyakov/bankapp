package com.example.bankapp.accounts.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@Table("users")
public class User {
    @Id
    private Long id;

    @Column
    private String username;

    @Column
    private String password;

    @Column
    private String firstName;

    @Column
    private String lastName;

    @Column
    private LocalDate birthDate;

    @Column
    private String email;

    @Column
    @CreatedDate
    private Instant createdAt;

    @Column
    @LastModifiedDate
    private Instant updatedAt;
}
