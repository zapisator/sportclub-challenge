package com.sportclub.challenge.adapter.out.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sportclub.challenge.adapter.in.web.dto.response.ErrorResponseDto;
import com.sportclub.challenge.application.port.out.log.LoggingPort;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final LoggingPort logger;
    private final ObjectMapper objectMapper;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException, ServletException {

        logger.error("Unauthorized error: {}", authException.getMessage());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        final ErrorResponseDto errorDto = new ErrorResponseDto(
                Instant.now().toEpochMilli(),
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                "Authentication required. " + authException.getMessage(),
                request.getRequestURI()
        );
        response.getOutputStream().println(objectMapper.writeValueAsString(errorDto));
    }
}