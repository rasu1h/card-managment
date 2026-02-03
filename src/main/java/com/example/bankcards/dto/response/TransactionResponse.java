package com.example.bankcards.dto.response;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponse {

    private UUID id;

    /**
     * Сумма перевода
     */
    private BigDecimal amount;

    /**
     * Карта отправителя (маскированная)
     */
    private TransactionCardInfo fromCard;

    /**
     * Карта получателя (маскированная)
     */
    private TransactionCardInfo toCard;

    /**
     * Дата и время транзакции
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant createdAt;

    /**
     * Тип транзакции
     */
    private String transactionType;

    /**
     * Статус
     */
    private String status;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TransactionCardInfo {
        private Long cardId;
        private String maskedCardNumber;
        private String lastFour;
    }
}