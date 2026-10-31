
package com.fintrack.controller;

import com.fintrack.dto.ProfileResponse;
import com.fintrack.dto.UpdateUsernameRequest;
import com.fintrack.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getMyProfile() {
        return ResponseEntity.ok(userService.getMyProfile());
    }

    @PatchMapping("/me")
    public ResponseEntity<ProfileResponse> updateMyUsername(
            @Valid @RequestBody UpdateUsernameRequest request) {

        return ResponseEntity.ok(
                userService.updateMyUsername(request)
        );
    }
}
