package com.example.bankapp.notifications.controller;

import com.example.bankapp.notifications.controller.dto.NotifyRequest;
import com.example.bankapp.notifications.service.NotificationsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class NotificationsController {
    private final NotificationsService service;

    @PostMapping("notify")
    public ResponseEntity<Void> notify(@RequestBody @Valid NotifyRequest request) {
        service.notify(request.email(), request.text(), request.idempotencyKey());

        return ResponseEntity.noContent().build();
    }
}
