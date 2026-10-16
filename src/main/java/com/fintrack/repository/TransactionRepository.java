package com.fintrack.repository;

import com.fintrack.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {


    boolean existsByFromAccount_IdOrToAccount_Id(
            Long fromAccountId,
            Long toAccountId
    );

}
