package com.example.bankapp.accounts.mapper;

import com.example.bankapp.accounts.controller.dto.request.UserRegisterRequest;
import com.example.bankapp.accounts.controller.dto.request.UserUpdateRequest;
import com.example.bankapp.accounts.controller.dto.response.UserCredentialsResponse;
import com.example.bankapp.accounts.controller.dto.response.UserResponse;
import com.example.bankapp.accounts.model.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toEntity(UserRegisterRequest dto);

    User toEntity(UserUpdateRequest request);

    UserResponse toUserResponseDto(User entity);

    UserCredentialsResponse toUserCredentialsResponseDto(User entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void patchEntity(User source, @MappingTarget User target);
}
