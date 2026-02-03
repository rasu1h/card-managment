package com.example.bankcards.dto.requests;


import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferRequest {

    @NotNull(message = "ID карты отправителя обязателен")
    @Positive(message = "ID карты отправителя должен быть положительным")
    private Long fromCardId;

    @NotNull(message = "ID карты получателя обязателен")
    @Positive(message = "ID карты получателя должен быть положительным")
    private Long toCardId;

    @NotNull(message = "Сумма перевода обязательна")
    @DecimalMin(value = "0.01", message = "Сумма должна быть больше 0")
    @DecimalMax(value = "1000000.00", message = "Сумма превышает максимально допустимую")
    @Digits(integer = 10, fraction = 2, message = "Некорректный формат суммы")
    private BigDecimal amount;
}