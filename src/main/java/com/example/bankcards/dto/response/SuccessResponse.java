package com.example.bankcards.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.Instant;
import io.swagger.v3.oas.annotations.media.Schema;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Успешный ответ от сервера")
public class SuccessResponse<T> {

    @Schema(description = "Статус успешности операции", example = "true")
    private boolean success;

    @Schema(description = "Сообщение о результате операции", example = "Операция выполнена успешно")
    private String message;

    @Schema(description = "Данные ответа")
    private T data;

    @Schema(description = "Временная метка ответа", example = "2024-02-04T10:30:00Z")
    private Instant timestamp;

    public static <T> SuccessResponse<T> of(String message, T data) {
        return SuccessResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(Instant.now())
                .build();
    }
}
