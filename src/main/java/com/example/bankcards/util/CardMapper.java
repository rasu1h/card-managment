package com.example.bankcards.util;

import com.example.bankcards.dto.response.CardResponse;
import com.example.bankcards.entity.Card;
import org.springframework.stereotype.Component;

import java.time.YearMonth;

@Component
public class CardMapper {

    public CardResponse toResponse(Card card) {
        return CardResponse.builder()
                .id(card.getId())
                .maskedCardNumber(maskCardNumber(card.getLastFour()))
                .lastFour(card.getLastFour())
                .owner(CardResponse.CardOwnerInfo.builder()
                        .id(card.getOwner().getId())
                        .fullName(card.getOwner().getUsername())
                        .build())
                .expiryDate(card.getExpiryDate())
                .status(card.getStatus())
                .balance(card.getBalance())
                .isExpired(card.getExpiryDate().isBefore(YearMonth.now()))
                .build();
    }

    private String maskCardNumber(String lastFour) {
        return "**** **** **** " + lastFour;
    }
}