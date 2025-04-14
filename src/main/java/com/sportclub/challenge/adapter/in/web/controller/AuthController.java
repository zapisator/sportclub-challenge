package com.sportclub.challenge.adapter.in.web.controller;

import com.sportclub.challenge.adapter.in.web.dto.request.LoginRequestDto;
import com.sportclub.challenge.adapter.in.web.dto.response.ErrorResponseDto;
import com.sportclub.challenge.adapter.in.web.dto.response.JwtResponseDto;
import com.sportclub.challenge.adapter.in.web.mapper.UserWebMapper; // Импортируем маппер
import com.sportclub.challenge.application.port.in.LoginUseCase;
import com.sportclub.challenge.application.port.in.command.LoginCommand;
import com.sportclub.challenge.application.port.out.log.LoggingPort; // Импортируем логгер
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
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user authentication")
public class AuthController {

    private final LoginUseCase loginUseCase;
    private final UserWebMapper userWebMapper;
    private final LoggingPort logger;

    @Operation(summary = "User Login", description = "Authenticates a user based on DNI and returns a JWT token if successful.")
    @ApiResponse(responseCode = "200", description = "Login successful, JWT token returned",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = JwtResponseDto.class)))
    @ApiResponse(responseCode = "400", description = "Invalid DNI format or missing DNI",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponseDto.class)))
    @ApiResponse(responseCode = "401", description = "Authentication failed (invalid DNI or user denied/disabled)",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponseDto.class)))
    @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponseDto.class)))
    @PostMapping("/login")
    public ResponseEntity<JwtResponseDto> login(@Valid @RequestBody LoginRequestDto requestDto) {
        logger.info("Login request received for DNI (masked): {}", maskDni(requestDto.dni()));
        final LoginCommand command = userWebMapper.requestToCommand(requestDto);
        final String jwtToken = loginUseCase.login(command);
        final JwtResponseDto responseDto = new JwtResponseDto(jwtToken);
        logger.info("Login successful for DNI (masked): {}", maskDni(requestDto.dni()));
        return ResponseEntity.ok(responseDto);
    }

    private String maskDni(String dni) {
        if (dni == null || dni.length() <= 4) {
            return "****";
        }
        return dni.substring(0, dni.length() - 4) + "****";
    }
}
