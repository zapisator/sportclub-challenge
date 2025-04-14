package com.sportclub.challenge.application.service;

import com.sportclub.challenge.application.exception.SportClubAccessDeniedException;
import com.sportclub.challenge.application.exception.InvalidDniFormatException;
import com.sportclub.challenge.application.exception.UserNotFoundException;
import com.sportclub.challenge.application.port.in.ValidateAccessUseCase;
import com.sportclub.challenge.application.port.out.log.LoggingPort;
import com.sportclub.challenge.application.port.out.persistence.target.TargetUserRepositoryPort;
import com.sportclub.challenge.domain.model.user.User;
import com.sportclub.challenge.domain.model.user.UserState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AccessValidationService implements ValidateAccessUseCase {

    private final TargetUserRepositoryPort targetUserRepositoryPort;
    private final LoggingPort logger;
    private static final Pattern DNI_PATTERN = Pattern.compile("^\\d{7,9}$");

    @Override
    @Transactional(value = "targetTransactionManager", readOnly = true)
    public User validateAccessByDni(String dni) {
        logger.info("Attempting access validation for DNI: {}", dni);

        if (dni == null || !DNI_PATTERN.matcher(dni).matches()) {
            String message = "Invalid DNI format provided: " + dni;
            logger.warn(message);
            throw new InvalidDniFormatException(message);
        }

        final User user = targetUserRepositoryPort.findByDni(dni)
                .orElseThrow(() -> {
                    String message = "User with DNI " + dni + " not found.";
                    logger.warn(message);
                    return new UserNotFoundException(message);
                });

        if (UserState.DENIED.equals(user.state())) {
            String message = "Access denied for user with DNI " + dni + ". User state is DENIED.";
            logger.warn(message);
            throw new SportClubAccessDeniedException(message);
        }

        if (UserState.AUTHORIZED.equals(user.state())) {
            logger.info("Access granted for user with DNI: {}. State: {}", dni, user.state());
            return user;
        } else {
            String message = "Access denied for user with DNI " + dni + ". Unknown or unsupported state: " + user.state();
            logger.error(message);
            throw new SportClubAccessDeniedException(message);
        }
    }
}

