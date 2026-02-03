package com.example.bankcards.controller;

import com.example.bankcards.dto.requests.AuthRequest;
import com.example.bankcards.dto.requests.RegisterAdminRequest;
import com.example.bankcards.dto.response.AuthResponse;
import com.example.bankcards.dto.response.SuccessResponse;
import com.example.bankcards.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register/admin")
    @Operation(summary = "Регистрация администратора (требуется специальный код)")
    public ResponseEntity<SuccessResponse<Void>> registerAdmin(
            @Valid @RequestBody RegisterAdminRequest request) {
        authService.registerAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponse.of("Администратор успешно зарегистрирован", null));
    }
    @PostMapping("/register")
    public ResponseEntity<SuccessResponse<Void>> register(@RequestBody AuthRequest request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponse.of("Пользователь успешно зарегистрирован", null));
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest request) {
        return authService.login(request);
    }
}
