package com.sportclub.challenge.adapter.in.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to validate user access")
public record AccessRequestDto(
        @Schema(
                description = "User's national ID number (DNI)",
                example = "11222333",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "DNI cannot be blank")
        @Size(min = 7, max = 9, message = "DNI must be between 7 and 9 digits")
        @Pattern(regexp = "\\d+", message = "DNI must contain only digits")
        String dni
) {}
