package com.example.bankcards.dto.response;


import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ответ с JWT токеном")
public record AuthResponse(
        @Schema(
                description = "JWT токен для авторизации",
                example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJqb2huX2RvZSIsImlhdCI6MTUxNjIzOTAyMn0..."
        )
        String token
) {
}