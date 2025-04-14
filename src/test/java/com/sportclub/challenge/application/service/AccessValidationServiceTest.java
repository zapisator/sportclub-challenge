package com.sportclub.challenge.application.service;

import com.sportclub.challenge.application.exception.InvalidDniFormatException;
import com.sportclub.challenge.application.exception.SportClubAccessDeniedException;
import com.sportclub.challenge.application.exception.UserNotFoundException;
import com.sportclub.challenge.application.port.out.log.LoggingPort;
import com.sportclub.challenge.application.port.out.persistence.target.TargetUserRepositoryPort;
import com.sportclub.challenge.domain.model.branch.Branch;
import com.sportclub.challenge.domain.model.user.User;
import com.sportclub.challenge.domain.model.user.UserState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.contains;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Tests for AccessValidationService")
class AccessValidationServiceTest {

    @Mock
    private TargetUserRepositoryPort targetUserRepositoryPort;
    @Mock
    private LoggingPort logger;
    @InjectMocks
    private AccessValidationService accessValidationService;
    private static final String VALID_DNI = "12345678";
    private static final String INVALID_DNI_FORMAT = "123ABC78";
    private static final String NON_EXISTENT_DNI = "99999999";
    private static final String DENIED_DNI = "11111111";
    private User authorizedUser;
    private User deniedUser;

    @BeforeEach
    void setUp() {
        final Branch testBranch = new Branch("B001", "Test Branch", "123 Test St", "Test City");
        authorizedUser = new User(
                "U001", "Auth", "User", "auth@test.com", "111", VALID_DNI,
                UserState.AUTHORIZED, testBranch
        );
        deniedUser = new User(
                "U002", "Denied", "User", "denied@test.com", "222", DENIED_DNI,
                UserState.DENIED, testBranch
        );
    }

    @Nested
    @DisplayName("DNI Format Validation")
    class DniFormatValidation {

        @Test
        @DisplayName("Should throw InvalidDniFormatException for null DNI")
        void shouldThrowExceptionForNullDni() {
            assertThatThrownBy(() -> accessValidationService.validateAccessByDni(null))
                    .isInstanceOf(InvalidDniFormatException.class)
                    .hasMessageContaining("Invalid DNI format provided: null");
            verify(logger).warn(eq("Invalid DNI format provided: null"));
            verify(targetUserRepositoryPort, never()).findByDni(anyString());
        }

        @Test
        @DisplayName("Should throw InvalidDniFormatException for invalid format DNI")
        void shouldThrowExceptionForInvalidFormatDni() {
            assertThatThrownBy(() -> accessValidationService.validateAccessByDni(INVALID_DNI_FORMAT))
                    .isInstanceOf(InvalidDniFormatException.class)
                    .hasMessageContaining("Invalid DNI format provided: " + INVALID_DNI_FORMAT);
            verify(logger).warn(eq("Invalid DNI format provided: " + INVALID_DNI_FORMAT));
            verify(targetUserRepositoryPort, never()).findByDni(anyString());
        }

        @Test
        @DisplayName("Should throw InvalidDniFormatException for DNI too short")
        void shouldThrowExceptionForShortDni() {
            final String shortDni = "123456";
            assertThatThrownBy(() -> accessValidationService.validateAccessByDni(shortDni))
                    .isInstanceOf(InvalidDniFormatException.class);
            verify(logger).warn(eq("Invalid DNI format provided: " + shortDni));
            verify(targetUserRepositoryPort, never()).findByDni(anyString());
        }

        @Test
        @DisplayName("Should throw InvalidDniFormatException for DNI too long")
        void shouldThrowExceptionForLongDni() {
            final String longDni = "1234567890";
            assertThatThrownBy(() -> accessValidationService.validateAccessByDni(longDni))
                    .isInstanceOf(InvalidDniFormatException.class);
            verify(logger).warn(eq("Invalid DNI format provided: " + longDni));
            verify(targetUserRepositoryPort, never()).findByDni(anyString());
        }
    }

    @Nested
    @DisplayName("User Existence Check")
    class UserExistenceCheck {

        @Test
        @DisplayName("Should throw UserNotFoundException when user does not exist")
        void shouldThrowUserNotFoundException() {
            when(targetUserRepositoryPort.findByDni(NON_EXISTENT_DNI)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> accessValidationService.validateAccessByDni(NON_EXISTENT_DNI))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessageContaining("User with DNI " + NON_EXISTENT_DNI + " not found.");
            verify(logger).info(contains("Attempting access"), eq(NON_EXISTENT_DNI));
            verify(targetUserRepositoryPort).findByDni(NON_EXISTENT_DNI);
            verify(logger).warn(contains("User with DNI " + NON_EXISTENT_DNI + " not found."));
        }
    }

    @Nested
    @DisplayName("User State Check")
    class UserStateCheck {

        @Test
        @DisplayName("Should throw SportClubAccessDeniedException when user state is DENIED")
        void shouldThrowAccessDeniedExceptionForDeniedState() {
            when(targetUserRepositoryPort.findByDni(DENIED_DNI)).thenReturn(Optional.of(deniedUser));

            assertThatThrownBy(() -> accessValidationService.validateAccessByDni(DENIED_DNI))
                    .isInstanceOf(SportClubAccessDeniedException.class)
                    .hasMessageContaining("Access denied for user with DNI " + DENIED_DNI + ". User state is DENIED.");
            verify(logger).info(contains("Attempting access"), eq(DENIED_DNI));
            verify(targetUserRepositoryPort).findByDni(DENIED_DNI);
            verify(logger).warn(contains("Access denied for user with DNI " + DENIED_DNI));
        }
    }

    @Nested
    @DisplayName("Successful Access Validation")
    class SuccessfulValidation {

        @Test
        @DisplayName("Should return the User when DNI is valid, user exists, and state is AUTHORIZED")
        void shouldReturnUserOnSuccessfulValidation() {
            when(targetUserRepositoryPort.findByDni(VALID_DNI)).thenReturn(Optional.of(authorizedUser));

            final User resultUser = accessValidationService.validateAccessByDni(VALID_DNI);

            assertThat(resultUser).isNotNull();
            assertThat(resultUser.dni()).isEqualTo(VALID_DNI);
            assertThat(resultUser.state()).isEqualTo(UserState.AUTHORIZED);
            assertThat(resultUser).isEqualTo(authorizedUser);
            verify(logger).info(contains("Attempting access"), eq(VALID_DNI));
            verify(targetUserRepositoryPort).findByDni(VALID_DNI);
            verify(logger).info(contains("Access granted for user with DNI"), eq(VALID_DNI), eq(UserState.AUTHORIZED));
            verify(logger, never()).warn(anyString(), any());
            verify(logger, never()).error(anyString(), any());
            verify(logger, never()).error(anyString(), any(Throwable.class), any());
        }
    }
}