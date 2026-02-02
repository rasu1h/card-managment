package com.example.bankcards.entity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.YearMonth;

@Entity
@Table(name = "cards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 🔐 Зашифрованный номер карты
     */
    @Column(name = "card_number_encrypted", nullable = false, unique = true, length = 512)
    private String cardNumberEncrypted;

    /**
     * Последние 4 цифры (НЕ шифруем)
     * Нужны для маскирования и поиска
     */
    @Column(name = "last_four", nullable = false, length = 4)
    private String lastFour;

    /**
     * Владелец карты
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    /**
     * Срок действия
     */
    @Column(name = "expiry_date", nullable = false)
    private YearMonth expiryDate;

    /**
     * Статус карты
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardStatus status;

    /**
     * Баланс
     */
    @Column(nullable = false)
    private BigDecimal balance;

}
