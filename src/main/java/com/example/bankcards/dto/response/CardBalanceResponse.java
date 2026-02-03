package com.example.bankcards.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardBalanceResponse {

    private Long cardId;
    private String maskedCardNumber;
    private BigDecimal balance;
    private String currency;
}