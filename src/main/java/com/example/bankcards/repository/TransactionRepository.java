package com.example.bankcards.repository;

import com.example.bankcards.entity.Transactions;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transactions, UUID> {

    @Query("SELECT t FROM Transactions t " +
            "WHERE t.fromCard.owner.id = :userId OR t.toCard.owner.id = :userId")
    Page<Transactions> findAllByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT t FROM Transactions t " +
            "WHERE t.fromCard.id = :cardId OR t.toCard.id = :cardId")
    Page<Transactions> findAllByCardId(@Param("cardId") Long cardId, Pageable pageable);
}