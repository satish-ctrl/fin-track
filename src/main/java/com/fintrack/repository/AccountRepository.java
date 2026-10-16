package com.fintrack.repository;

import com.fintrack.entity.Account;
import com.fintrack.enums.AccountStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    List<Account> findAllByUser_EmailAndStatus(
            String email,
            AccountStatus status
    );

    Optional<Account> findByIdAndUser_Email(
            Long accountId,
            String email
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    SELECT a FROM Account a
    WHERE a.id = :accountId
      AND a.user.email = :email
""")
    Optional<Account> findByIdAndUserEmailForUpdate(
            @Param("accountId") Long accountId,
            @Param("email") String email
    );

    boolean existsByUser_EmailAndStatusAndBalanceNot(
            String email,
            AccountStatus status,
            BigDecimal balance
    );
}
