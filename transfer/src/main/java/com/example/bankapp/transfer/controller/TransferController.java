package com.example.bankapp.transfer.controller;

import com.example.bankapp.transfer.controller.dto.TransferRequest;
import com.example.bankapp.transfer.model.Transfer;
import com.example.bankapp.transfer.saga.TransferSagaOrchestrator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/transfer")
public class TransferController {
    private final TransferSagaOrchestrator sagaOrchestrator;

    @PostMapping
    public ResponseEntity<Void> transfer(@RequestBody @Valid TransferRequest request,
                                         @RequestHeader(name = "Idempotency-Key") UUID idempotencyKey) {
        Transfer transfer = Transfer.builder()
                .idempotencyKey(idempotencyKey)
                .from(Transfer.AccountSnapshot.builder()
                        .accountId(request.fromAccountId())
                        .build())
                .to(Transfer.AccountSnapshot.builder()
                        .accountId(request.toAccountId())
                        .build())
                .sourceAmount(request.amount())
                .build();
        sagaOrchestrator.startTransfer(transfer);

        return ResponseEntity.noContent().build();
    }
}
