package com.example.bankapp.accounts.mapper;

import com.example.bankapp.accounts.controller.dto.request.BankAccountCreateRequest;
import com.example.bankapp.accounts.controller.dto.response.BankAccountResponse;
import com.example.bankapp.accounts.model.BankAccount;
import com.example.bankapp.accounts.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.jdbc.core.mapping.AggregateReference;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BankAccountMapper {
    @Mapping(target = "user", source = "userId")
    BankAccount toEntity(BankAccountCreateRequest dto);

    @Mapping(target = "userId", source = "user.id")
    BankAccountResponse toBankAccountResponseDto(BankAccount entity);

    List<BankAccountResponse> toBankAccountResponseDtoList(List<BankAccount> entities);

    default AggregateReference<User, Long> toUserReference(Long userId) {
        return userId != null ? AggregateReference.to(userId) : null;
    }
}
