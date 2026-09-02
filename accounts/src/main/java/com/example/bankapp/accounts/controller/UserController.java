package com.example.bankapp.accounts.controller;

import com.example.bankapp.accounts.controller.dto.request.PasswordUpdateRequest;
import com.example.bankapp.accounts.controller.dto.request.UserRegisterRequest;
import com.example.bankapp.accounts.controller.dto.request.UserUpdateRequest;
import com.example.bankapp.accounts.controller.dto.response.UserCredentialsResponse;
import com.example.bankapp.accounts.controller.dto.response.UserResponse;
import com.example.bankapp.accounts.mapper.UserMapper;
import com.example.bankapp.accounts.model.User;
import com.example.bankapp.accounts.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class UserController {
    private final UserMapper userMapper;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponse> registerUser(@RequestBody @Valid UserRegisterRequest request) {
        User saved = userService.create(userMapper.toEntity(request));
        UserResponse response = userMapper.toUserResponse(saved);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        User user = userService.findById(id);
        UserResponse response = userMapper.toUserResponse(user);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{username}/credentials")
    public ResponseEntity<UserCredentialsResponse> getUserCredentials(@PathVariable String username) {
        User user = userService.findByUsername(username);
        UserCredentialsResponse response = userMapper.toUserCredentialsResponse(user);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id,
                                                   @RequestBody @Valid UserUpdateRequest request) {
        User entity = userMapper.toEntity(request);
        entity.setId(id);
        User updated = userService.update(entity);
        UserResponse response = userMapper.toUserResponse(updated);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/password")
    public ResponseEntity<Void> updateUserPassword(@PathVariable Long id,
                                                   @RequestBody @Valid PasswordUpdateRequest request) {
        userService.updatePassword(id, request.newPassword());

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
