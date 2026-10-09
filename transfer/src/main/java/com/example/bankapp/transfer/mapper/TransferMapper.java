package com.example.bankapp.transfer.mapper;

import com.example.bankapp.transfer.controller.dto.TransferRequest;
import com.example.bankapp.transfer.model.Transfer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransferMapper {
    @Mapping(source = "amount", target = "sourceAmount")
    Transfer toEntity(TransferRequest request);
}
