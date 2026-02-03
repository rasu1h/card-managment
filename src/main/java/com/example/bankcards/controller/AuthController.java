package com.example.bankcards.controller;

import com.example.bankcards.dto.requests.AuthRequest;
import com.example.bankcards.dto.requests.RegisterAdminRequest;
import com.example.bankcards.dto.response.AuthResponse;
import com.example.bankcards.dto.response.ErrorResponse;
import com.example.bankcards.dto.response.SuccessResponse;
import com.example.bankcards.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Validated
@Tag(
        name = "Authentication",
        description = "Endpoints для аутентификации, регистрации и управления пользователями"
)
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register/admin")
    @Operation(
            summary = "Регистрация администратора",
            description = "Создает нового администратора. Требуется специальный секретный код администратора."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Администратор успешно зарегистрирован",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "success": true,
                                              "message": "Администратор успешно зарегистрирован",
                                              "data": null,
                                              "timestamp": "2024-02-04T10:30:00Z"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Неверные данные или неправильный код администратора",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "timestamp": "2024-02-04T10:30:00Z",
                                              "status": 400,
                                              "error": "Bad Request",
                                              "message": "Неверный код администратора",
                                              "path": "/auth/register/admin"
                                            }
                                            """
                            )
                    )
            )
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Данные для регистрации администратора",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = RegisterAdminRequest.class),
                    examples = @ExampleObject(
                            value = """
                                    {
                                      "username": "admin",
                                      "password": "admin123",
                                      "phoneNumber": "+77001234567",
                                      "email": "admin@example.com",
                                      "fullName": "Admin User",
                                      "adminCode": "ADMIN_SECRET_2024"
                                    }
                                    """
                    )
            )
    )
    public ResponseEntity<SuccessResponse<Void>> registerAdmin(
            @Valid @RequestBody RegisterAdminRequest request) {
        authService.registerAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponse.of("Администратор успешно зарегистрирован", null));
    }

    @PostMapping("/register")
    @Operation(
            summary = "Регистрация пользователя",
            description = "Создает нового пользователя с ролью USER"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Пользователь успешно зарегистрирован",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "success": true,
                                              "message": "Пользователь успешно зарегистрирован",
                                              "data": null,
                                              "timestamp": "2024-02-04T10:30:00Z"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Неверные данные или пользователь уже существует",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "timestamp": "2024-02-04T10:30:00Z",
                                              "status": 400,
                                              "error": "Bad Request",
                                              "message": "Пользователь с таким именем уже существует",
                                              "path": "/auth/register"
                                            }
                                            """
                            )
                    )
            )
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Данные для регистрации пользователя",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AuthRequest.class),
                    examples = @ExampleObject(
                            value = """
                                    {
                                      "username": "john_doe",
                                      "password": "password123",
                                      "phoneNumber": "+77005555555"
                                    }
                                    """
                    )
            )
    )
    public ResponseEntity<SuccessResponse<Void>> register(
            @Valid @RequestBody AuthRequest request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponse.of("Пользователь успешно зарегистрирован", null));
    }

    @PostMapping("/login")
    @Operation(
            summary = "Вход в систему",
            description = "Аутентификация пользователя и получение JWT токена"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешная аутентификация",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Неверные учетные данные",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "timestamp": "2024-02-04T10:30:00Z",
                                              "status": 401,
                                              "error": "Unauthorized",
                                              "message": "Неверное имя пользователя или пароль",
                                              "path": "/auth/login"
                                            }
                                            """
                            )
                    )
            )
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Учетные данные для входа",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AuthRequest.class),
                    examples = @ExampleObject(
                            value = """
                                    {
                                      "username": "john_doe",
                                      "password": "password123",
                                      "phoneNumber": "+77005555555"
                                    }
                                    """
                    )
            )
    )
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}