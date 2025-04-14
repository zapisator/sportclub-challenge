package com.sportclub.challenge.adapter.in.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Generic success message response")
public record SuccessMessageDto(
        @Schema(description = "Success message", example = "Acceso permitido")
        String message
) {}