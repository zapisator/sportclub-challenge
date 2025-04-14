package com.sportclub.challenge.infrastructure.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.jwt")
@Validated
public record JwtProperties(

                             @NotBlank(message = "JWT secret (app.jwt.secret) cannot be blank")
                             @Size(min = 32, message = "JWT secret must be at least 32 bytes long for HS256 security")
                             String secret,

                             @Min(value = 300000, message = "JWT expiration (app.jwt.expiration-ms) must be at least 5 minutes (300000 ms)")
                             long expirationMs
) {
}
