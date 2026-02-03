package com.example.bankcards.controller;

import com.example.bankcards.dto.response.CardBalanceResponse;
import com.example.bankcards.dto.response.CardResponse;
import com.example.bankcards.dto.response.PageResponse;
import com.example.bankcards.dto.response.SuccessResponse;
import com.example.bankcards.service.AuthService;
import com.example.bankcards.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
@Validated
@Tag(name = "Cards", description = "API для управления банковскими картами")
@SecurityRequirement(name = "Bearer Authentication")

public class CardController {

    private final CardService cardService;
    private final AuthService authService;

    // ==================== ADMIN endpoints ====================

    @PostMapping("/admin/create")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Создать карту для пользователя (ADMIN)")
    public ResponseEntity<SuccessResponse<CardResponse>> createCard(
            @RequestParam @Min(1) Long ownerId) {

        CardResponse response = cardService.createCard(ownerId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponse.of("Карта успешно создана", response));
    }

    @PostMapping("/admin/{cardId}/block")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Заблокировать карту (ADMIN)")
    public ResponseEntity<SuccessResponse<CardResponse>> blockCard(
            @PathVariable Long cardId,
            @RequestParam(required = false) String reason) {

        CardResponse response = cardService.blockCard(cardId, reason);
        return ResponseEntity.ok(SuccessResponse.of("Карта заблокирована", response));
    }

    @PostMapping("/admin/{cardId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Активировать карту (ADMIN)")
    public ResponseEntity<SuccessResponse<CardResponse>> activateCard(@PathVariable Long cardId) {
        CardResponse response = cardService.activateCard(cardId);
        return ResponseEntity.ok(SuccessResponse.of("Карта активирована", response));
    }

    @DeleteMapping("/admin/{cardId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Удалить карту (ADMIN)")
    public ResponseEntity<SuccessResponse<Void>> deleteCard(@PathVariable Long cardId) {
        cardService.deleteCard(cardId);
        return ResponseEntity.ok(SuccessResponse.of("Карта удалена", null));
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Получить все карты (ADMIN)")
    public ResponseEntity<PageResponse<CardResponse>> getAllCards(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {

        return ResponseEntity.ok(cardService.getAllCards(page, size));
    }

    @PostMapping("/admin/{cardId}/top-up")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Пополнить баланс карты (ADMIN)")
    public ResponseEntity<SuccessResponse<Void>> topUpCard(
            @PathVariable Long cardId,
            @RequestParam @Min(1) BigDecimal amount) {

        cardService.topUpCard(cardId, amount);
        return ResponseEntity.ok(SuccessResponse.of("Баланс пополнен", null));
    }

    // ==================== USER endpoints ====================

    @GetMapping("/my")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Получить мои карты")
    public ResponseEntity<PageResponse<CardResponse>> getMyCards(
            Authentication authentication,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {

        Long userId = authService.getUserIdFromAuthentication();
        return ResponseEntity.ok(cardService.getUserCards(userId, page, size));
    }

    @GetMapping("/my/{cardId}/balance")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Получить баланс карты")
    public ResponseEntity<CardBalanceResponse> getCardBalance(
            Authentication authentication,
            @PathVariable Long cardId) {

        Long userId = authService.getUserIdFromAuthentication();
        return ResponseEntity.ok(cardService.getCardBalance(userId, cardId));
    }

    @PostMapping("/my/{cardId}/block")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Заблокировать мою карту")
    public ResponseEntity<SuccessResponse<CardResponse>> blockMyCard(
            Authentication authentication,
            @PathVariable Long cardId,
            @RequestParam(required = false) String reason) {

        Long userId = authService.getUserIdFromAuthentication();
        CardResponse response = cardService.requestCardBlock(userId, cardId, reason);
        return ResponseEntity.ok(SuccessResponse.of("Карта заблокирована", response));
    }

    @GetMapping("/my/search")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Поиск моих карт по последним 4 цифрам")
    public ResponseEntity<PageResponse<CardResponse>> searchMyCards(
            Authentication authentication,
            @RequestParam(required = false) String lastFour,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {

        Long userId = authService.getUserIdFromAuthentication();
        return ResponseEntity.ok(cardService.searchUserCards(userId, lastFour, page, size));
    }


}