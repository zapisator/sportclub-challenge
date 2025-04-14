package com.sportclub.challenge.application.service;

import com.sportclub.challenge.application.exception.AccountStatusException;
import com.sportclub.challenge.application.exception.AuthenticationFailedException;
import com.sportclub.challenge.application.port.in.command.LoginCommand;
import com.sportclub.challenge.application.port.out.log.LoggingPort;
import com.sportclub.challenge.application.port.out.persistence.target.TargetUserRepositoryPort;
import com.sportclub.challenge.application.port.out.security.JwtProviderPort;
import com.sportclub.challenge.domain.model.branch.Branch;
import com.sportclub.challenge.domain.model.user.User;
import com.sportclub.challenge.domain.model.user.UserState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Tests for AuthenticationService")
class AuthenticationServiceTest {

    @Mock
    private TargetUserRepositoryPort userRepositoryPort;
    @Mock
    private JwtProviderPort jwtProviderPort;
    @Mock
    private LoggingPort logger;

    @InjectMocks
    private AuthenticationService authenticationService;

    private LoginCommand loginCommandAuthorized;
    private LoginCommand loginCommandDenied;
    private LoginCommand loginCommandNotFound;
    private User authorizedUser;
    private User deniedUser;
    private final String DUMMY_TOKEN = "dummy.jwt.token";

    @BeforeEach
    void setUp() {
        Branch testBranch = new Branch("B001", "Test Branch", "123 Test St", "Test City");
        authorizedUser = User.builder()
                .id("U001")
                .firstName("Auth")
                .lastName("User")
                .email("auth@test.com")
                .phone("111")
                .dni("12345678")
                .state(UserState.AUTHORIZED)
                .branch(testBranch)
                .build();
        deniedUser = User.builder()
                .id("U002")
                .firstName("Denied")
                .lastName("User")
                .email("denied@test.com")
                .phone("222")
                .dni("87654321")
                .state(UserState.DENIED)
                .branch(testBranch)
                .build();
        loginCommandAuthorized = new LoginCommand("12345678");
        loginCommandDenied = new LoginCommand("87654321");
        loginCommandNotFound = new LoginCommand("00000000");
    }

    @Test
    @DisplayName("should return JWT token when login is successful for authorized user")
    void login_success() {
        when(userRepositoryPort.findByDni(authorizedUser.dni())).thenReturn(Optional.of(authorizedUser));
        when(jwtProviderPort.generateToken(authorizedUser.dni())).thenReturn(DUMMY_TOKEN);

        final String token = authenticationService.login(loginCommandAuthorized);

        assertThat(token).isEqualTo(DUMMY_TOKEN);
        verify(logger).info("Login attempt for DNI: {}", authorizedUser.dni());
        verify(userRepositoryPort).findByDni(authorizedUser.dni());
        verify(jwtProviderPort).generateToken(authorizedUser.dni());
        verify(logger).info("Login successful for DNI: {}. Token generated.", authorizedUser.dni());
        verifyNoMoreInteractions(logger, userRepositoryPort, jwtProviderPort);
    }

    @Test
    @DisplayName("should throw BadCredentialsException when user is not found")
    void login_userNotFound_throwsBadCredentialsException() {
        when(userRepositoryPort.findByDni(loginCommandNotFound.dni())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authenticationService.login(loginCommandNotFound))
                .isInstanceOf(AuthenticationFailedException.class)
                .hasMessageContaining("Authentication failed: Invalid credentials for DNI: " + loginCommandNotFound.dni());
        verify(logger).info("Login attempt for DNI: {}", loginCommandNotFound.dni());
        verify(userRepositoryPort).findByDni(loginCommandNotFound.dni());
        verify(logger).warn("Authentication failed: Invalid credentials for DNI: " + loginCommandNotFound.dni());
        verifyNoInteractions(jwtProviderPort);
        verify(logger, never()).info(contains("Login successful"));
    }

    @Test
    @DisplayName("should throw DisabledException when user state is DENIED")
    void login_userDenied_throwsDisabledException() {
        when(userRepositoryPort.findByDni(deniedUser.dni())).thenReturn(Optional.of(deniedUser));

        assertThatThrownBy(() -> authenticationService.login(loginCommandDenied))
                .isInstanceOf(AccountStatusException.class)
                .hasMessageContaining("Authentication failed: User account with DNI " + deniedUser.dni() + " is denied access.");
        verify(logger).info("Login attempt for DNI: {}", deniedUser.dni());
        verify(userRepositoryPort).findByDni(deniedUser.dni());
        verify(logger).warn(eq("Authentication failed: User account with DNI " + deniedUser.dni() + " is denied access."));
        verifyNoInteractions(jwtProviderPort);
        verify(logger, never()).info(contains("Login successful"));
    }
}