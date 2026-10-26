package com.example.bankapp.blocker.controller;

import com.example.bankapp.blocker.controller.dto.BlockerRequest;
import com.example.bankapp.blocker.controller.dto.BlockerResponse;
import com.example.bankapp.blocker.service.BlockerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/blocker")
public class BlockerController {
    private final BlockerService blockerService;

    @PostMapping
    public ResponseEntity<BlockerResponse> checkAccountOperation(@RequestBody @Valid BlockerRequest request) {
        boolean shouldBlock = blockerService.shouldBlock(request.accountId());

        return ResponseEntity.ok(new BlockerResponse(shouldBlock));
    }
}
