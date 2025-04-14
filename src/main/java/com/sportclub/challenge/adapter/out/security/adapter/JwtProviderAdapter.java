package com.sportclub.challenge.adapter.out.security.adapter;

import com.sportclub.challenge.application.port.out.log.LoggingPort;
import com.sportclub.challenge.application.port.out.security.JwtProviderPort;
import com.sportclub.challenge.infrastructure.config.JwtProperties;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtProviderAdapter implements JwtProviderPort {

    private final LoggingPort logger;
    private final JwtProperties jwtProperties;
    private SecretKey jwtSecretKey;

    @PostConstruct
    public void init() {
        try {
            this.jwtSecretKey = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
            logger.info("JWT Secret Key initialized successfully.");
        } catch (Exception e) {
            logger.error("!!! CRITICAL: Failed to initialize JWT Secret Key. Check app.jwt.secret property format and length !!!", e);
        }
    }

    @Override
    public String generateToken(String dni) {
        if (jwtSecretKey == null) {
            logger.error("Cannot generate token: JWT Secret Key is not initialized!");
            throw new IllegalStateException("JWT Secret Key not initialized");
        }
        final Date now = new Date();
        final Date expiryDate = new Date(now.getTime() + jwtProperties.expirationMs());
        final String token = Jwts.builder()
                .subject(dni)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(jwtSecretKey)
                .compact();
        logger.debug("Generated JWT token for DNI: {}", dni);
        return token;
    }

    @Override
    public boolean validateToken(String token) {
        if (jwtSecretKey == null) {
            logger.error("Cannot validate token: JWT Secret Key is not initialized!");
            return false;
        }
        if (token == null || token.isBlank()) {
            logger.warn("Validation failed: Token string is null or empty.");
            return false;
        }
        try {
            Jwts.parser()
                    .verifyWith(jwtSecretKey)
                    .build()
                    .parseSignedClaims(token);
            logger.debug("Token validation successful.");
            return true;
        } catch (SignatureException e) {
            logger.error("Validation failed: Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("Validation failed: Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("Validation failed: JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("Validation failed: JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("Validation failed: JWT claims string is empty: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Validation failed: Unexpected error: {}", e.getMessage(), e);
        }
        return false;
    }

    @Override
    public Optional<String> getDniFromToken(String token) {
        if (jwtSecretKey == null) {
            logger.error("Cannot get DNI: JWT Secret Key is not initialized!");
            return Optional.empty();
        }
        if (token == null || token.isBlank()) {
            logger.warn("Cannot get DNI: Token string is null or empty.");
            return Optional.empty();
        }
        try {
            final String dni = Jwts.parser()
                    .verifyWith(jwtSecretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
            logger.debug("Extracted DNI from token: {}", dni);
            return Optional.ofNullable(dni);
        } catch (JwtException e) {
            logger.error("Cannot get DNI: Error parsing token: {}", e.getMessage());
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Cannot get DNI: Unexpected error during parsing: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }
}