package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    Page<Card> findByOwnerId(Long ownerId, Pageable pageable);

    Optional<Card> findByIdAndOwnerId(Long id, Long ownerId);

    Optional<Card> findByCardNumberEncrypted(String encryptedNumber);

    boolean existsByCardNumberEncrypted(String encryptedNumber);

    /**
     * Получить карту с пессимистической блокировкой для безопасных транзакций
     * Предотвращает race conditions при одновременных операциях с балансом
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Card c WHERE c.id = :id")
    Optional<Card> findByIdWithLock(@Param("id") Long id);

    Page<Card> findByOwnerAndLastFourContaining(User owner, String lastFour, Pageable pageable);
    /**
     * Получить карту с блокировкой, проверяя владельца
     * Используется для транзакций между своими картами
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Card c WHERE c.id = :id AND c.owner.id = :ownerId")
    Optional<Card> findByIdAndOwnerIdWithLock(@Param("id") Long id, @Param("ownerId") Long ownerId);

    Page<Card> findByOwner(User owner, Pageable pageable);
}