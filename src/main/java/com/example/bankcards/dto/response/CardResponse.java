package com.example.bankcards.dto.response;

import lombok.Builder;

import java.math.BigDecimal;


import com.example.bankcards.entity.CardStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.YearMonth;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardResponse {

    private Long id;

    /**
     * Маскированный номер карты: **** **** **** 1234
     */
    private String maskedCardNumber;

    /**
     * Последние 4 цифры
     */
    private String lastFour;

    /**
     * Владелец карты
     */
    private CardOwnerInfo owner;

    /**
     * Срок действия (MM/yyyy)
     */
    @JsonFormat(pattern = "MM/yyyy")
    private YearMonth expiryDate;

    /**
     * Статус карты
     */
    private CardStatus status;

    /**
     * Баланс
     */
    private BigDecimal balance;

    /**
     * Истек ли срок
     */
    private Boolean isExpired;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CardOwnerInfo {
        private Long id;
        private String fullName;
    }
}