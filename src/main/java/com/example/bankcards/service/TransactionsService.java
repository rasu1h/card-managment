package com.example.bankcards.service;

import com.example.bankcards.dto.requests.TransferRequest;
import com.example.bankcards.dto.response.PageResponse;
import com.example.bankcards.dto.response.TransactionResponse;
import com.example.bankcards.entity.Transactions;

import java.math.BigDecimal;
import java.util.List;


public interface    TransactionsService {
    TransactionResponse transferBetweenOwnCards(Long userId, TransferRequest request);
    PageResponse<TransactionResponse> getMyTransactions(Long userId, int page, int size);
}
