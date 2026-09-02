package org.example.bankapp.front.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.bankapp.front.integration.accounts.dto.request.UserRegisterRequest;
import org.example.bankapp.front.integration.accounts.exceptions.AccountsServiceException;
import org.example.bankapp.front.integration.accounts.AccountsGateway;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@RequiredArgsConstructor
@Controller
public class AuthController {
    private final AccountsGateway accountsGateway;

    @GetMapping("/login")
    public String showLogin() {
        return "login";
    }

    @GetMapping("/register")
    public String showRegistration(Model model) {
        model.addAttribute("userRegisterRequest", new UserRegisterRequest());

        return "registration";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute UserRegisterRequest userRegisterRequest,
                               BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "registration";
        }

        try {
            accountsGateway.registerUser(userRegisterRequest);
        } catch (AccountsServiceException e) {
            bindingResult.reject("error.user.registration", e.getMessage());
            return "registration";
        }

        return "redirect:/login";
    }
}
