package com.fintrack.service;

import com.fintrack.entity.PasswordResetToken;
import com.fintrack.entity.User;
import com.fintrack.enums.UserStatus;
import com.fintrack.repository.PasswordResetTokenRepository;
import com.fintrack.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class PasswordResetService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int TOKEN_BYTES = 32;
    private static final long TOKEN_EXPIRY_MINUTES = 15;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final String resetUrl;

    public PasswordResetService(
            UserRepository userRepository,
            PasswordResetTokenRepository tokenRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService,
            @Value("${app.password-reset.url}") String resetUrl
    ) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.resetUrl = resetUrl;
    }

    @Transactional
    public void requestPasswordReset(String email) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

        Optional<User> userOptional =
                userRepository.findByEmailAndStatus(
                        normalizedEmail,
                        UserStatus.ACTIVE
                );

        // Unknown/closed accounts ke liye email existence reveal mat karo.
        if (userOptional.isEmpty()) {
            return;
        }

        User user = userOptional.get();

        // Purane unused reset tokens invalidate karo.
        List<PasswordResetToken> oldTokens =
                tokenRepository.findAllByUser_IdAndUsedAtIsNull(
                        user.getId()
                );

        Instant now = Instant.now();
        oldTokens.forEach(token -> token.setUsedAt(now));
        tokenRepository.saveAll(oldTokens);

        // 32 cryptographically random bytes = 256-bit token.
        byte[] randomBytes = new byte[TOKEN_BYTES];
        SECURE_RANDOM.nextBytes(randomBytes);

        String rawToken = HexFormat.of().formatHex(randomBytes);
        String tokenHash = sha256(rawToken);

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUser(user);
        resetToken.setTokenHash(tokenHash);
        resetToken.setExpiresAt(
                now.plus(TOKEN_EXPIRY_MINUTES, ChronoUnit.MINUTES)
        );

        tokenRepository.saveAndFlush(resetToken);

        String link = resetUrl + "?token=" + rawToken;

        // SMTP failure par exception transaction ko rollback karne degi.
        emailService.sendEmail(
                normalizedEmail,
                "FinTrack - Password Reset",
                """
                We received a request to reset your FinTrack password.

                Open this link to set a new password:
                %s

                This link expires in 15 minutes and can only be used once.
                If you did not request this, ignore this email.
                """.formatted(link)
        );
    }

    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        String tokenHash = sha256(rawToken);

        PasswordResetToken token = tokenRepository
                .findByTokenHashForUpdate(tokenHash)
                .orElseThrow(PasswordResetService::invalidTokenException);

        Instant now = Instant.now();

        if (token.getUsedAt() != null
                || !token.getExpiresAt().isAfter(now)) {
            throw invalidTokenException();
        }

        User user = token.getUser();

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw invalidTokenException();
        }

        // Never store a raw password.
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Current token aur user ke baaki unused reset tokens invalidate karo.
        token.setUsedAt(now);

        List<PasswordResetToken> otherTokens =
                tokenRepository.findAllByUser_IdAndUsedAtIsNull(
                        user.getId()
                );

        otherTokens.forEach(other -> other.setUsedAt(now));
        tokenRepository.saveAll(otherTokens);
    }

    private static String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            // SHA-256 Java platform par available hona chahiye.
            throw new IllegalStateException(
                    "SHA-256 is not available",
                    exception
            );
        }
    }

    private static ResponseStatusException invalidTokenException() {
        return new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Invalid or expired password reset token"
        );
    }
}