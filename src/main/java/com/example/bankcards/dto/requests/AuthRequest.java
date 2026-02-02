package com.example.bankcards.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AuthRequest(

        @NotBlank
        @Pattern(
                regexp = "^\\+?[1-9]\\d{9,14}$",
                message = "Invalid phone number format"
        )
        String phoneNumber,

        @NotBlank
        @Size(min = 3, max = 50)
        String username,

        @NotBlank
        @Size(min = 8, max = 100)
        String password
) {}
