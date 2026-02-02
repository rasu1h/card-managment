package com.example.bankcards.util;

import com.example.bankcards.dto.response.CardResponse;
import com.example.bankcards.entity.Card;

public class CardMapper {
    public static CardResponse toDto(Card card) {
        return new CardResponse(
                card.getId().toString(),
                CardMaskUtil.mask(card.getLastFour()),
                card.getBalance(),
                card.getStatus().name()
        );
    }

}
