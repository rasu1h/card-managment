package com.example.bankcards.dto.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Запрос на регистрацию администратора")
public class RegisterAdminRequest {

    @Schema(
            description = "Имя пользователя администратора",
            example = "admin",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Имя пользователя обязательно")
    @Size(min = 3, max = 50, message = "Имя пользователя должно быть от 3 до 50 символов")
    private String username;

    @Schema(
            description = "Пароль администратора",
            example = "admin123",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Пароль обязателен")
    @Size(min = 6, max = 100, message = "Пароль должен быть от 6 до 100 символов")
    private String password;

    @Schema(
            description = "Номер телефона",
            example = "+77001234567",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Номер телефона обязателен")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Некорректный формат номера телефона")
    private String phoneNumber;

    @Schema(
            description = "Email администратора (опционально)",
            example = "admin@example.com"
    )
    @Email(message = "Некорректный формат email")
    private String email;

    @Schema(
            description = "Полное имя администратора (опционально)",
            example = "Admin User"
    )
    @Size(min = 2, max = 100, message = "Полное имя должно быть от 2 до 100 символов")
    private String fullName;

    @Schema(
            description = "Секретный код для регистрации администратора",
            example = "ADMIN_SECRET_2024",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Код администратора обязателен")
    private String adminCode;
}