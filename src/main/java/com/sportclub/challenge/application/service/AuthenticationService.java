package com.sportclub.challenge.application.service;

import com.sportclub.challenge.application.exception.AccountStatusException;
import com.sportclub.challenge.application.exception.AuthenticationFailedException;
import com.sportclub.challenge.application.port.in.LoginUseCase;
import com.sportclub.challenge.application.port.in.command.LoginCommand;
import com.sportclub.challenge.application.port.out.log.LoggingPort;
import com.sportclub.challenge.application.port.out.persistence.target.TargetUserRepositoryPort;
import com.sportclub.challenge.application.port.out.security.JwtProviderPort;
import com.sportclub.challenge.domain.model.user.User;
import com.sportclub.challenge.domain.model.user.UserState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthenticationService implements LoginUseCase {

    private final TargetUserRepositoryPort userRepositoryPort;
    private final JwtProviderPort jwtProviderPort;
    private final LoggingPort logger;

    @Override
    @Transactional(value = "targetTransactionManager", readOnly = true)
    public String login(LoginCommand command) {
        final String dni = command.dni();
        logger.info("Login attempt for DNI: {}", dni);

        final User user = userRepositoryPort.findByDni(dni)
                .orElseThrow(() -> {
                    final String message = "Authentication failed: Invalid credentials for DNI: " + dni;
                    logger.warn(message);
                    return new AuthenticationFailedException(message);
                });

        if (UserState.DENIED.equals(user.state())) {
            final String message = "Authentication failed: User account with DNI " + dni + " is denied access.";
            logger.warn(message);
            throw new AccountStatusException(message);
        }

        final String token = jwtProviderPort.generateToken(user.dni());
        logger.info("Login successful for DNI: {}. Token generated.", dni);
        return token;
    }
}
