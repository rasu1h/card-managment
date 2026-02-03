package com.example.bankcards.service.impl;

import com.example.bankcards.dto.requests.TransferRequest;
import com.example.bankcards.dto.response.PageResponse;
import com.example.bankcards.dto.response.TransactionResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.Transactions;
import com.example.bankcards.exception.CardNotActiveException;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.util.TransactionMapper;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransactionRepository;
import com.example.bankcards.service.TransactionsService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional
public class TransactionsServiceImpl implements TransactionsService {

    private final TransactionRepository transactionRepository;
    private final CardRepository cardRepository;
    private final TransactionMapper transactionMapper;

    @Override
    public TransactionResponse transferBetweenOwnCards(Long userId, TransferRequest request) {
        // Проверка: карты должны быть разными
        if (request.getFromCardId().equals(request.getToCardId())) {
            throw new IllegalArgumentException("Нельзя переводить на ту же карту");
        }

        // Получаем карты с блокировкой
        Card fromCard = cardRepository.findByIdWithLock(request.getFromCardId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Карта отправителя не найдена: " + request.getFromCardId()));

        Card toCard = cardRepository.findByIdWithLock(request.getToCardId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Карта получателя не найдена: " + request.getToCardId()));

        // Проверка собственности обеих карт
        if (!fromCard.getOwner().getId().equals(userId) || !toCard.getOwner().getId().equals(userId)) {
            throw new IllegalArgumentException("Обе карты должны принадлежать вам");
        }

        // Проверка статуса карт
        validateCardStatus(fromCard, "отправителя");
        validateCardStatus(toCard, "получателя");

        // Проверка достаточности средств
        if (fromCard.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientFundsException(
                    String.format("Недостаточно средств. Доступно: %s, требуется: %s",
                            fromCard.getBalance(), request.getAmount())
            );
        }

        // Выполняем перевод
        fromCard.setBalance(fromCard.getBalance().subtract(request.getAmount()));
        toCard.setBalance(toCard.getBalance().add(request.getAmount()));

        cardRepository.save(fromCard);
        cardRepository.save(toCard);

        // Создаем запись транзакции
        Transactions transaction = Transactions.builder()
                .amount(request.getAmount())
                .fromCard(fromCard)
                .toCard(toCard)
                .createdAt(Instant.now())
                .build();

        Transactions savedTransaction = transactionRepository.save(transaction);

        return transactionMapper.toResponse(savedTransaction);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TransactionResponse> getMyTransactions(Long userId, int page, int size) {
        validatePagination(page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Transactions> transactionsPage = transactionRepository.findAllByUserId(userId, pageable);

        return mapToPageResponse(transactionsPage);
    }

    // Helper methods

    private void validateCardStatus(Card card, String cardRole) {
        if (card.getStatus() != CardStatus.ACTIVE) {
            throw new CardNotActiveException(
                    String.format("Карта %s неактивна. Статус: %s", cardRole, card.getStatus())
            );
        }
    }

    private void validatePagination(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("Номер страницы не может быть отрицательным");
        }
        if (size <= 0 || size > 100) {
            throw new IllegalArgumentException("Размер страницы должен быть от 1 до 100");
        }
    }

    private PageResponse<TransactionResponse> mapToPageResponse(Page<Transactions> transactionsPage) {
        return PageResponse.<TransactionResponse>builder()
                .content(transactionsPage.getContent().stream()
                        .map(transactionMapper::toResponse)
                        .toList())
                .pageNumber(transactionsPage.getNumber())
                .pageSize(transactionsPage.getSize())
                .totalElements(transactionsPage.getTotalElements())
                .totalPages(transactionsPage.getTotalPages())
                .first(transactionsPage.isFirst())
                .last(transactionsPage.isLast())
                .empty(transactionsPage.isEmpty())
                .build();
    }
}