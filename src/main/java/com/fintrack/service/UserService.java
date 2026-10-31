package com.fintrack.service;

import com.fintrack.dto.ProfileResponse;
import com.fintrack.dto.RegisterRequest;
import com.fintrack.dto.RegisterResponse;
import com.fintrack.dto.UpdateUsernameRequest;
import com.fintrack.entity.Account;
import com.fintrack.entity.User;
import com.fintrack.enums.AccountStatus;
import com.fintrack.enums.UserStatus;
import com.fintrack.repository.AccountRepository;
import com.fintrack.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccountRepository accountRepository;
    private final CurrentUserService currentUserService;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AccountRepository accountRepository,
            CurrentUserService currentUserService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.accountRepository = accountRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {

        String username = request.getUsername().trim();
        String email = request.getEmail()
                .trim()
                .toLowerCase(Locale.ROOT);

        if (userRepository.existsByUsernameAndStatus(
                username, UserStatus.ACTIVE)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Username already exists"
            );
        }

        if (userRepository.existsByEmailAndStatus(
                email, UserStatus.ACTIVE)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email already registered"
            );
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(
                passwordEncoder.encode(request.getPassword())
        );
        user.setStatus(UserStatus.ACTIVE);

        User savedUser = userRepository.save(user);

        return new RegisterResponse(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail()
        );
    }

    public ProfileResponse getMyProfile() {

        User user = currentUserService.getCurrentActiveUser();

        return toProfileResponse(user);
    }

    @Transactional
    public ProfileResponse updateMyUsername(
            UpdateUsernameRequest request) {

        User user = currentUserService.getCurrentActiveUser();

        String username = request.username().trim();

        if (username.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Username is required"
            );
        }

        if (username.equals(user.getUsername())) {
            return toProfileResponse(user);
        }

        if (userRepository.existsByUsername(username)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Username already exists"
            );
        }

        user.setUsername(username);

        User savedUser = userRepository.save(user);

        return toProfileResponse(savedUser);
    }

    private ProfileResponse toProfileResponse(User user) {
        return new ProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail()
        );
    }

    @Transactional
    public void closeMyAccount() {

        User user = currentUserService.getCurrentActiveUser();

        List<Account> activeAccounts =
                accountRepository.findAllByUser_EmailAndStatus(
                        user.getEmail(),
                        AccountStatus.ACTIVE
                );

        boolean hasNonZeroBalance = activeAccounts.stream()
                .anyMatch(account ->
                        account.getBalance().compareTo(BigDecimal.ZERO) != 0
                );

        if (hasNonZeroBalance) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Please transfer or spend the remaining balance before closing your account"
            );
        }

        activeAccounts.forEach(account ->
                account.setStatus(AccountStatus.INACTIVE)
        );

        accountRepository.saveAll(activeAccounts);

        user.setStatus(UserStatus.CLOSED);
        userRepository.save(user);
    }
}
