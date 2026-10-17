package com.fintrack.controller;

import com.fintrack.dto.*;
import com.fintrack.entity.User;
import com.fintrack.service.LoginService;
import com.fintrack.service.PasswordResetService;
import com.fintrack.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final LoginService loginService;
    private final PasswordResetService passwordResetService;

    public AuthController(UserService userService,
                          LoginService loginService,
                          PasswordResetService passwordResetService) {
        this.userService = userService;
        this.loginService = loginService;
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        RegisterResponse response = userService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        LoginResponse response = loginService.login(request);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id){
        User user = userService.getUser(id);

        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> closeMyAccount() {
        userService.closeMyAccount();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        passwordResetService.requestPasswordReset(request.email());

        return ResponseEntity.ok(
                new MessageResponse(
                        "If an active account exists for this email, "
                                + "password reset instructions will be sent."
                )
        );
    }

    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        passwordResetService.resetPassword(
                request.token(),
                request.newPassword()
        );

        return ResponseEntity.ok(
                new MessageResponse("Password has been reset successfully.")
        );
    }

}
