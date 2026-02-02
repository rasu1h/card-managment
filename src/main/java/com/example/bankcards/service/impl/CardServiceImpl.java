package com.example.bankcards.service.impl;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.CardService;
import com.example.bankcards.util.CardNumberEncryptor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;
    private final CardNumberEncryptor cryptoService; // AES шифрование
    private final UserRepository userRepository;

    @Override
    public Card createCard(Long ownerId, String cardNumber, YearMonth expiryDate, BigDecimal balance) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String encrypted = cryptoService.convertToDatabaseColumn(cardNumber);

        Card card = Card.builder()
                .owner(owner)
                .cardNumberEncrypted(encrypted)
                .lastFour(cardNumber.substring(cardNumber.length() - 4))
                .expiryDate(expiryDate)
                .status(CardStatus.ACTIVE)
                .balance(balance)
                .build();

        return cardRepository.save(card);
    }
    @Override
    public void blockCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"));
        card.setStatus(CardStatus.BLOCKED);
        cardRepository.save(card);
    }
    @Override
    public void activateCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"));
        card.setStatus(CardStatus.ACTIVE);
        cardRepository.save(card);
    }

    @Override
    public List<Card> getUserCards(Long ownerId, Pageable pageable) {
        return cardRepository.findByOwnerId(ownerId, pageable);
    }

    @Override
    public List<Card> getAllCards(Pageable pageable) {
        return cardRepository.findAll(pageable).getContent();
    }

}
