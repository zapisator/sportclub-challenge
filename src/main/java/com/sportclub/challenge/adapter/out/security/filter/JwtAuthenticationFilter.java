package com.sportclub.challenge.adapter.out.security.filter;

import com.sportclub.challenge.application.port.out.log.LoggingPort;
import com.sportclub.challenge.application.port.out.security.JwtProviderPort;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProviderPort jwtProviderPort;
    private final UserDetailsService userDetailsService;
    private final LoggingPort logger;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            final String jwt = extractJwtFromRequest(request);

            if (StringUtils.hasText(jwt) && jwtProviderPort.validateToken(jwt)) {
                logger.debug("JWT Token is valid. Attempting to authenticate...");
                jwtProviderPort.getDniFromToken(jwt).ifPresent(dni -> {
                    logger.debug("Extracted DNI {} from token. Loading UserDetails...", dni);

                    try {
                        final UserDetails userDetails = userDetailsService.loadUserByUsername(dni);
                        logger.debug("UserDetails loaded successfully for DNI: {}", dni);
                        final UsernamePasswordAuthenticationToken authentication
                                = new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        logger.info("Successfully authenticated user with DNI: {} via JWT. " +
                                "Security context updated.", dni
                        );

                    } catch (Exception e) {
                        logger.error("Could not set user authentication in security context for DNI {}: {}", dni, e.getMessage(), e);
                        SecurityContextHolder.clearContext();
                    }
                });
            } else {
                if (StringUtils.hasText(jwt)) {
                    logger.debug("JWT Token found but validation failed for URI: {}", request.getRequestURI());
                } else {
                    logger.trace("No JWT token found in request header for URI: {}", request.getRequestURI());
                }
            }
        } catch (Exception e) {
            logger.error("Could not set user authentication in security context: {}", e.getMessage(), e);
        }

        filterChain.doFilter(request, response);
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        logger.trace("Authorization header missing or does not contain Bearer token.");
        return null;
    }
}
