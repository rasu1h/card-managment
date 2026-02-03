package com.example.bankcards.controller;
import com.example.bankcards.service.AuthService;
import com.example.bankcards.service.TransactionsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.bankcards.dto.requests.TransferRequest;
import com.example.bankcards.dto.response.PageResponse;
import com.example.bankcards.dto.response.SuccessResponse;
import com.example.bankcards.dto.response.TransactionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Validated
@Tag(name = "Transactions", description = "API для управления транзакциями")
@SecurityRequirement(name = "Bearer Authentication")
public class TransactionsController {

    private final TransactionsService transactionsService;
    private final AuthService authService;

    @PostMapping("/transfer")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Перевод между своими картами")
    public ResponseEntity<SuccessResponse<TransactionResponse>> transferBetweenOwnCards(
            Authentication authentication,
            @Valid @RequestBody TransferRequest request) {

        Long userId = authService.getUserIdFromAuthentication();
        TransactionResponse response = transactionsService.transferBetweenOwnCards(userId, request);

        return ResponseEntity.ok(
                SuccessResponse.of("Перевод успешно выполнен", response)
        );
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Получить историю моих транзакций")
    public ResponseEntity<PageResponse<TransactionResponse>> getMyTransactions(
            Authentication authentication,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {

        Long userId = authService.getUserIdFromAuthentication();
        PageResponse<TransactionResponse> response = transactionsService.getMyTransactions(userId, page, size);

        return ResponseEntity.ok(response);
    }


}
