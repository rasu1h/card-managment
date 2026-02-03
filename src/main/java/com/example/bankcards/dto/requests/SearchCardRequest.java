package com.example.bankcards.dto.requests;
import jakarta.validation.constraints.*;
import lombok.*;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchCardRequest {

    @Pattern(regexp = "^[0-9]{4}$", message = "Последние 4 цифры должны содержать ровно 4 цифры")
    private String lastFour;

    @Min(value = 0, message = "Номер страницы не может быть отрицательным")
    private Integer page = 0;

    @Min(value = 1, message = "Размер страницы должен быть минимум 1")
    @Max(value = 100, message = "Размер страницы не должен превышать 100")
    private Integer size = 10;
}
