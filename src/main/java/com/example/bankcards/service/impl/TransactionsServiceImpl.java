package com.example.bankcards.service.impl;

import com.example.bankcards.entity.Transactions;
import com.example.bankcards.service.TransactionsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionsServiceImpl implements TransactionsService {

    @Override
    public Transactions transfer(Long fromCardId, Long toCardId, BigDecimal amount) {
        return null;
    }

    @Override
    public List<Transactions> getUserTransactions(Long userId, int page, int size) {
        return List.of();
    }


}
