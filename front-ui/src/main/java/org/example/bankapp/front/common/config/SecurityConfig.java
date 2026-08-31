package org.example.bankapp.front.common.config;

import io.github.wimdeblauwe.htmx.spring.boot.security.HxRefreshHeaderAuthenticationEntryPoint;
import org.example.bankapp.front.auth.AuthenticationFailureHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.RequestHeaderRequestMatcher;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   AuthenticationFailureHandler authenticationFailureHandler) {
        var entryPoint = new HxRefreshHeaderAuthenticationEntryPoint();
        var requestMatcher = new RequestHeaderRequestMatcher("HX-Request");

        return http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/login", "/register", "/error").permitAll()
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .failureHandler(authenticationFailureHandler))
                .exceptionHandling(handler -> handler
                        .defaultAuthenticationEntryPointFor(entryPoint, requestMatcher))
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
