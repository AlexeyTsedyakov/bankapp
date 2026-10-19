package com.example.bankapp.accounts.service.event;

import com.example.bankapp.accounts.model.User;

public record UserCreated(
        User user
) {
}
