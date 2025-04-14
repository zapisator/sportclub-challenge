package com.sportclub.challenge.adapter.in.web.dto.response;

import com.sportclub.challenge.domain.model.user.UserState;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "User Information")
public record UserDto(
        @Schema(description = "Unique identifier of the user", example = "U001")
        String id,

        @Schema(description = "User's first name", example = "Juan")
        String firstName,

        @Schema(description = "User's last name", example = "Pérez")
        String lastName,

        @Schema(description = "User's email address", example = "juanperez@mail.com")
        String email,

        @Schema(description = "User's phone number", example = "123456789")
        String phone,

        @Schema(description = "User's national ID number (DNI)", example = "11222333")
        String dni,

        @Schema(description = "User's access status", example = "AUTHORIZED")
        UserState state,

        @Schema(description = "Branch the user belongs to")
        BranchDto branch
) {}
