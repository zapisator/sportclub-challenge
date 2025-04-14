package com.sportclub.challenge.adapter.in.web.controller;

import com.sportclub.challenge.adapter.in.web.dto.request.AccessRequestDto;
import com.sportclub.challenge.adapter.in.web.dto.response.ErrorResponseDto;
import com.sportclub.challenge.adapter.in.web.dto.response.SuccessMessageDto;
import com.sportclub.challenge.application.port.in.ValidateAccessUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/acceso")
@RequiredArgsConstructor
@Tag(name = "Access Validations", description = "Endpoints for validating user access based on DNI")
public class AccessController {

    private final ValidateAccessUseCase validateAccessUseCase;

    @Operation(summary = "Validate user access", description = "Checks if a user with the given DNI exists and is authorized to access.")
    @ApiResponse(responseCode = "200", description = "Access granted",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = SuccessMessageDto.class)))
    @ApiResponse(responseCode = "400", description = "Invalid DNI format",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponseDto.class)))
    @ApiResponse(responseCode = "403", description = "Access denied (user state is DENIED)",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponseDto.class)))
    @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponseDto.class)))
    @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponseDto.class)))
    @PostMapping
    public ResponseEntity<SuccessMessageDto> validateAccess(
            @Valid @RequestBody AccessRequestDto requestDto
    ) {
        validateAccessUseCase.validateAccessByDni(requestDto.dni());
        return ResponseEntity.ok(new SuccessMessageDto("Acceso permitido"));
    }
}
