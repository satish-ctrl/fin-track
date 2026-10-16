package com.fintrack.service;

import com.fintrack.dto.LoginRequest;
import com.fintrack.dto.LoginResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class LoginService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public LoginService(
            AuthenticationManager authenticationManager,
            JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {

        String email = request.getEmail()
                .trim()
                .toLowerCase(Locale.ROOT);

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        email,
                        request.getPassword()
                )
        );

        String token = jwtService.generateToken(email);

        return new LoginResponse(token, "Bearer");
    }
}
