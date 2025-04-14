package com.sportclub.challenge.adapter.in.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response containing the JWT token upon successful login")
public record JwtResponseDto(
        @Schema(description = "JSON Web Token", example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIi...")
        String token
) {}