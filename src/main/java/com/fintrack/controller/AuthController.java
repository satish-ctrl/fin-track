package com.fintrack.controller;

import com.fintrack.dto.LoginRequest;
import com.fintrack.dto.LoginResponse;
import com.fintrack.dto.RegisterRequest;
import com.fintrack.dto.RegisterResponse;
import com.fintrack.entity.User;
import com.fintrack.service.LoginService;
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

    public AuthController(UserService userService,
                          LoginService loginService) {
        this.userService = userService;
        this.loginService = loginService;
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

}
