package com.example.bankcards.dto.response;

import java.math.BigDecimal;

public record CardResponse(
        String id,
        String cardNumber,
        BigDecimal balance,
        String status
) {}
