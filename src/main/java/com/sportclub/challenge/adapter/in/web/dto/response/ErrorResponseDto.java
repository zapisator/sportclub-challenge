package com.sportclub.challenge.adapter.in.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Standard error response format")
public record ErrorResponseDto(
        @Schema(description = "Timestamp of the error occurrence (Unix milliseconds)", example = "1678886400000")
        long timestamp,

        @Schema(description = "HTTP status code", example = "404")
        int status,

        @Schema(description = "General error category", example = "Not Found")
        String error,

        @Schema(description = "Specific error message detailing the issue", example = "User with DNI 12345678 not found")
        String message,

        @Schema(description = "The path where the error occurred", example = "/acceso")
        String path
) {}