package com.example.bankcards.controller;
import com.example.bankcards.dto.requests.AuthRequest;
import com.example.bankcards.dto.response.AuthResponse;
import com.example.bankcards.entity.Transactions;
import com.example.bankcards.service.TransactionsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionsController {

    private final TransactionsService transactionsService;

    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(@RequestParam Long fromCardId,
                                      @RequestParam Long toCardId,
                                      @RequestParam BigDecimal amount) {
        Transactions tx = transactionsService.transfer(fromCardId, toCardId, amount);
        return ResponseEntity.ok(tx);
    }

    @GetMapping
    public ResponseEntity<List<Transactions>> getUserTransactions(@RequestParam Long userId,
                                                                  @RequestParam int page,
                                                                  @RequestParam int size) {
        return ResponseEntity.ok(transactionsService.getUserTransactions(userId, page, size));
    }
}
