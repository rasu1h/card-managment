package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

public interface CardService {
     Card createCard(Long ownerId, String cardNumber, YearMonth expiryDate, BigDecimal balance);
     void blockCard(Long cardId);
    void activateCard(Long cardId);
    List<Card> getUserCards(Long ownerId, Pageable pageable);
    List<Card> getAllCards(Pageable pageable);
    }
