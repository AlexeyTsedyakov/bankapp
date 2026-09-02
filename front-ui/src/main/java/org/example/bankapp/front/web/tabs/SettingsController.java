package org.example.bankapp.front.web.tabs;

import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxResponseHeader;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.bankapp.front.auth.UserDetailsImpl;
import org.example.bankapp.front.integration.accounts.dto.request.PasswordUpdateRequest;
import org.example.bankapp.front.integration.accounts.dto.request.UserUpdateRequest;
import org.example.bankapp.front.integration.accounts.dto.response.UserResponse;
import org.example.bankapp.front.integration.accounts.exceptions.AccountsServiceException;
import org.example.bankapp.front.integration.accounts.AccountsGateway;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@Controller
@RequestMapping("/settings")
public class SettingsController {
    private final AccountsGateway accountsGateway;
    private final SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();

    @HxRequest
    @GetMapping
    public String getSettingsTab(Model model, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long userId = userDetails.getId();
        try {
            UserResponse user = accountsGateway.getUser(userId);
            model.addAttribute("userUpdateRequest", createUserUpdateRequest(user));
        } catch (AccountsServiceException e) {
            model.addAttribute("accountsServiceErrorMessage", e.getMessage());
            model.addAttribute("userUpdateRequest", new UserUpdateRequest());
        }
        model.addAttribute("passwordUpdateRequest", new PasswordUpdateRequest());

        return "fragments/tabs/settings :: settings-tab";
    }

    @HxRequest
    @PutMapping("/user")
    public String updateUser(@AuthenticationPrincipal UserDetailsImpl userDetails,
                             @Valid @ModelAttribute UserUpdateRequest userUpdateRequest,
                             BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "fragments/tabs/settings.html :: profile-card";
        }

        try {
            accountsGateway.updateUser(userDetails.getId(), userUpdateRequest);
        } catch (AccountsServiceException e) {
            model.addAttribute("accountsRequestErrorMessage", e.getMessage());
            return "fragments/tabs/settings :: profile-card";
        }

        model.addAttribute("userUpdatedMessage", "Профиль успешно обновлен!");
        return "fragments/tabs/settings :: profile-card";
    }

    @HxRequest
    @PatchMapping("/user/password")
    public String updateUserPassword(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                     @Valid @ModelAttribute PasswordUpdateRequest passwordUpdateRequest,
                                     BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "fragments/tabs/settings :: change-password-card";
        }

        try {
            accountsGateway.updateUserPassword(userDetails.getId(), passwordUpdateRequest);
        } catch (RuntimeException e) {
            model.addAttribute("accountsRequestErrorMessage", e.getMessage());
            return "fragments/tabs/settings :: change-password-card";
        }

        model.addAttribute("passwordUpdatedMessage", "Пароль успешно изменен!");
        return "fragments/tabs/settings :: change-password-card";
    }

    @HxRequest
    @DeleteMapping("/user")
    public String deleteUser(@AuthenticationPrincipal UserDetailsImpl userDetails, Model model,
                             HttpServletResponse response, HttpServletRequest request,
                             Authentication authentication) {
        try {
            accountsGateway.deleteUser(userDetails.getId());
        } catch (AccountsServiceException e) {
            model.addAttribute("accountsRequestErrorMessage", e.getMessage());
            return "fragments/tabs/settings :: profile-error-notification";
        }

        logoutHandler.logout(request, response, authentication);

        response.setHeader(HtmxResponseHeader.HX_REDIRECT.getValue(), "/login?logout");
        return "redirect:/login?logout";
    }

    private UserUpdateRequest createUserUpdateRequest(UserResponse user) {
        return UserUpdateRequest.builder()
                .firstName(user.firstName())
                .lastName(user.lastName())
                .birthDate(user.birthDate())
                .email(user.email())
                .build();
    }
}
