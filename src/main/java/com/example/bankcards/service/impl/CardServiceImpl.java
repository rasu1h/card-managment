package com.example.bankcards.service.impl;

import com.example.bankcards.dto.response.CardBalanceResponse;
import com.example.bankcards.dto.response.CardResponse;
import com.example.bankcards.dto.response.PageResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.util.CardMapper;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.CardService;
import com.example.bankcards.util.CardNumberEncryptor;
import com.example.bankcards.util.CardNumberGenerator;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;

@Service
@RequiredArgsConstructor
@Transactional
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;
    private final CardNumberEncryptor cryptoService;
    private final UserRepository userRepository;
    private final CardNumberGenerator cardNumberGenerator;
    private final CardMapper cardMapper;

    @Override
    public CardResponse createCard(Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден: " + ownerId));

        String cardNumber = generateUniqueCardNumber();
        String encrypted = cryptoService.convertToDatabaseColumn(cardNumber);
        YearMonth expiryDate = YearMonth.now().plusYears(5);

        Card card = Card.builder()
                .owner(owner)
                .cardNumberEncrypted(encrypted)
                .lastFour(cardNumber.substring(cardNumber.length() - 4))
                .expiryDate(expiryDate)
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.ZERO)
                .build();

        Card savedCard = cardRepository.save(card);
        return cardMapper.toResponse(savedCard);
    }

    @Override
    public CardResponse blockCard(Long cardId, String reason) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new EntityNotFoundException("Карта не найдена: " + cardId));

        if (card.getStatus() == CardStatus.BLOCKED) {
            throw new IllegalStateException("Карта уже заблокирована");
        }

        card.setStatus(CardStatus.BLOCKED);
        // Здесь можно добавить поле blockedReason, если оно есть в Entity
        Card savedCard = cardRepository.save(card);

        return cardMapper.toResponse(savedCard);
    }

    @Override
    public CardResponse activateCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new EntityNotFoundException("Карта не найдена: " + cardId));

        if (card.getStatus() == CardStatus.ACTIVE) {
            throw new IllegalStateException("Карта уже активна");
        }

        card.setStatus(CardStatus.ACTIVE);
        Card savedCard = cardRepository.save(card);

        return cardMapper.toResponse(savedCard);
    }

    @Override
    public void deleteCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new EntityNotFoundException("Карта не найдена: " + cardId));

        card.setStatus(CardStatus.DELETED);
        cardRepository.save(card);
    }

    @Override
    public CardResponse requestCardBlock(Long userId, Long cardId, String reason) {
        Card card = cardRepository.findByIdAndOwnerId(cardId, userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Карта не найдена или не принадлежит пользователю"));

        if (card.getStatus() == CardStatus.BLOCKED) {
            throw new IllegalStateException("Карта уже заблокирована");
        }

        card.setStatus(CardStatus.BLOCKED);
        Card savedCard = cardRepository.save(card);

        return cardMapper.toResponse(savedCard);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CardResponse> getUserCards(Long userId, int page, int size) {
        validatePagination(page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Card> cardsPage = cardRepository.findByOwnerId(userId, pageable);

        return mapToPageResponse(cardsPage);
    }

    @Override
    @Transactional(readOnly = true)
    public CardBalanceResponse getCardBalance(Long userId, Long cardId) {
        Card card = cardRepository.findByIdAndOwnerId(cardId, userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Карта не найдена или не принадлежит пользователю"));

        return CardBalanceResponse.builder()
                .cardId(card.getId())
                .maskedCardNumber(maskCardNumber(card.getLastFour()))
                .balance(card.getBalance())
                .currency("KZT")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CardResponse> searchUserCards(Long userId, String lastFour, int page, int size) {
        validatePagination(page, size);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден: " + userId));

        Pageable pageable = PageRequest.of(page, size);
        Page<Card> cardsPage;

        if (lastFour != null && !lastFour.isEmpty()) {
            cardsPage = cardRepository.findByOwnerAndLastFourContaining(user, lastFour, pageable);
        } else {
            cardsPage = cardRepository.findByOwner(user, pageable);
        }

        return mapToPageResponse(cardsPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CardResponse> getAllCards(int page, int size) {
        validatePagination(page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Card> cardsPage = cardRepository.findAll(pageable);

        return mapToPageResponse(cardsPage);
    }

    @Override
    public void topUpCard(Long cardId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Сумма пополнения должна быть положительной");
        }

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new EntityNotFoundException("Карта не найдена: " + cardId));

        card.setBalance(card.getBalance().add(amount));
        cardRepository.save(card);
    }

    // Helper methods

    private String generateUniqueCardNumber() {
        String cardNumber;
        String encrypted;

        do {
            cardNumber = cardNumberGenerator.generateCardNumber();
            encrypted = cryptoService.convertToDatabaseColumn(cardNumber);
        } while (cardRepository.existsByCardNumberEncrypted(encrypted));

        return cardNumber;
    }

    private void validatePagination(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("Номер страницы не может быть отрицательным");
        }
        if (size <= 0 || size > 100) {
            throw new IllegalArgumentException("Размер страницы должен быть от 1 до 100");
        }
    }

    private String maskCardNumber(String lastFour) {
        return "**** **** **** " + lastFour;
    }

    private PageResponse<CardResponse> mapToPageResponse(Page<Card> cardsPage) {
        return PageResponse.<CardResponse>builder()
                .content(cardsPage.getContent().stream()
                        .map(cardMapper::toResponse)
                        .toList())
                .pageNumber(cardsPage.getNumber())
                .pageSize(cardsPage.getSize())
                .totalElements(cardsPage.getTotalElements())
                .totalPages(cardsPage.getTotalPages())
                .first(cardsPage.isFirst())
                .last(cardsPage.isLast())
                .empty(cardsPage.isEmpty())
                .build();
    }
}