package com.example.bankcards.util;

import com.example.bankcards.dto.response.TransactionResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transactions;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public TransactionResponse toResponse(Transactions transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .amount(transaction.getAmount())
                .fromCard(toCardInfo(transaction.getFromCard()))
                .toCard(toCardInfo(transaction.getToCard()))
                .createdAt(transaction.getCreatedAt())
                .transactionType("TRANSFER_BETWEEN_OWN_CARDS")
                .status("SUCCESS")
                .build();
    }

    private TransactionResponse.TransactionCardInfo toCardInfo(Card card) {
        return TransactionResponse.TransactionCardInfo.builder()
                .cardId(card.getId())
                .maskedCardNumber(maskCardNumber(card.getLastFour()))
                .lastFour(card.getLastFour())
                .build();
    }

    private String maskCardNumber(String lastFour) {
        return "**** **** **** " + lastFour;
    }
}