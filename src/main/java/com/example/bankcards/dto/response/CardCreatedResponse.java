package com.example.bankcards.dto.response;


import lombok.*;

import java.math.BigDecimal;
import java.time.YearMonth;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardCreatedResponse {

    private Long cardId;
    private String maskedCardNumber;
    private String lastFour;
    private YearMonth expiryDate;
    private BigDecimal initialBalance;
    private String status;
    private String message;
}