package com.example.bankcards.service;


import com.example.bankcards.dto.response.CardBalanceResponse;
import com.example.bankcards.dto.response.CardResponse;
import com.example.bankcards.dto.response.PageResponse;

import java.math.BigDecimal;

import java.math.BigDecimal;

public interface CardService {
    // ADMIN operations
    CardResponse createCard(Long ownerId);
    CardResponse blockCard(Long cardId, String reason);
    CardResponse activateCard(Long cardId);
    void deleteCard(Long cardId);
    PageResponse<CardResponse> getAllCards(int page, int size);
    void topUpCard(Long cardId, BigDecimal amount);

    // USER operations
    CardResponse requestCardBlock(Long userId, Long cardId, String reason);
    PageResponse<CardResponse> getUserCards(Long userId, int page, int size);
    CardBalanceResponse getCardBalance(Long userId, Long cardId);
    PageResponse<CardResponse> searchUserCards(Long userId, String lastFour, int page, int size);
}