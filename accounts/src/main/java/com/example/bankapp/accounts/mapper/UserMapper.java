package com.example.bankapp.accounts.mapper;

import com.example.bankapp.accounts.controller.dto.request.UserRegisterRequest;
import com.example.bankapp.accounts.controller.dto.request.UserUpdateRequest;
import com.example.bankapp.accounts.controller.dto.response.UserCredentialsResponse;
import com.example.bankapp.accounts.controller.dto.response.UserResponse;
import com.example.bankapp.accounts.model.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
    User toEntity(UserRegisterRequest registerRequest);

    User toEntity(UserUpdateRequest updateRequest);

    UserResponse toUserResponse(User entity);

    UserCredentialsResponse toUserCredentialsResponse(User entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void patchEntity(User source, @MappingTarget User target);
}
