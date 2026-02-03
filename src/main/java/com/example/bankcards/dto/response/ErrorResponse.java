package com.example.bankcards.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Ответ с ошибкой")
public class ErrorResponse {

    @Schema(description = "Временная метка ошибки", example = "2024-02-04T10:30:00Z")
    private Instant timestamp;

    @Schema(description = "HTTP статус код", example = "400")
    private int status;

    @Schema(description = "Название ошибки", example = "Bad Request")
    private String error;

    @Schema(description = "Описание ошибки", example = "Неверные данные запроса")
    private String message;

    @Schema(description = "Путь запроса", example = "/auth/register")
    private String path;

    @Schema(description = "Детали ошибки")
    private List<String> details;

    public static ErrorResponse of(int status, String error, String message, String path) {
        return ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status)
                .error(error)
                .message(message)
                .path(path)
                .build();
    }
}