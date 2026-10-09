package com.example.bankapp.transfer.controller;

import com.example.bankapp.transfer.controller.dto.TransferRequest;
import com.example.bankapp.transfer.mapper.TransferMapper;
import com.example.bankapp.transfer.model.Transfer;
import com.example.bankapp.transfer.service.TransferSagaOrchestrator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
public class TransferController {
    private final TransferSagaOrchestrator sagaOrchestrator;
    private final TransferMapper mapper;

    @PostMapping
    public ResponseEntity<Void> transfer(@RequestBody @Valid TransferRequest request,
                                         @RequestHeader(name = "Idempotency-Key") UUID idempotencyKey) {
        Transfer transfer = mapper.toEntity(request);
        transfer.setIdempotencyKey(idempotencyKey);
        sagaOrchestrator.startTransfer(transfer);

        return ResponseEntity.noContent().build();
    }
}
