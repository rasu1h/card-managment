package com.example.bankcards.service;

import com.example.bankcards.entity.Transactions;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.util.List;

public interface TransactionsService {
    Transactions transfer(Long fromCardId, Long toCardId, BigDecimal amount);

     List<Transactions> getUserTransactions(Long userId, int page, int size);
}
