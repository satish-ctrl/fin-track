package com.fintrack.repository;

import com.fintrack.entity.User;
import com.fintrack.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {


    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    Optional<User> findByEmailAndStatus(
            String email,
            UserStatus status
    );

    boolean existsByEmailAndStatus(String email, UserStatus status);

    boolean existsByUsernameAndStatus(String username, UserStatus status);
}
