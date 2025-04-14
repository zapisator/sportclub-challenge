package com.sportclub.challenge.adapter.in.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sportclub.challenge.adapter.in.web.dto.request.LoginRequestDto;
import com.sportclub.challenge.application.port.out.persistence.target.TargetBranchRepositoryPort;
import com.sportclub.challenge.application.port.out.persistence.target.TargetUserRepositoryPort;
import com.sportclub.challenge.application.port.out.security.JwtProviderPort;
import com.sportclub.challenge.domain.model.branch.Branch;
import com.sportclub.challenge.domain.model.user.User;
import com.sportclub.challenge.domain.model.user.UserState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Integration Tests for AuthController (/auth)")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AuthControllerIT {

    private static final String AUTHORIZED_DNI = "11111111";
    private static final String DENIED_DNI = "22222222";
    private static final String NOT_FOUND_DNI = "99999999";
    private static final String INVALID_DNI = "ABC";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private TargetUserRepositoryPort userRepositoryPort;
    @Autowired
    private TargetBranchRepositoryPort branchRepositoryPort;
    @Autowired
    private JwtProviderPort jwtProviderPort;

    @BeforeEach
    void setUpDatabase() {
        final Branch branch = branchRepositoryPort.save(new Branch(
                "B1", "IT Branch", "Tech Street", "Dev City"
        ));

        userRepositoryPort.save(User.builder()
                .id("U1")
                .firstName("Auth")
                .lastName("User")
                .email("auth@it.com")
                .phone("111")
                .dni(AUTHORIZED_DNI)
                .state(UserState.AUTHORIZED)
                .branch(branch)
                .build());
        userRepositoryPort.save(User.builder()
                .id("U2")
                .firstName("Denied")
                .lastName("User")
                .email("denied@it.com")
                .phone("222")
                .dni(DENIED_DNI)
                .state(UserState.DENIED)
                .branch(branch)
                .build());
    }

    @Test
    @DisplayName("POST /auth/login - Success (200 OK) for authorized user, returns valid JWT")
    void login_authorizedUser_shouldReturnOkWithJwt() throws Exception {
        final LoginRequestDto requestDto = new LoginRequestDto(AUTHORIZED_DNI);
        final MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token", is(notNullValue())))
                .andExpect(jsonPath("$.token", isA(String.class)))
                .andExpect(jsonPath("$.token", not(emptyString())))
                .andReturn();

        final String responseBody = result.getResponse().getContentAsString();
        final String token = objectMapper.readTree(responseBody).get("token").asText();

        assertThat(jwtProviderPort.validateToken(token)).isTrue();
        assertThat(jwtProviderPort.getDniFromToken(token)).isPresent().contains(AUTHORIZED_DNI);
    }

    @Test
    @DisplayName("POST /auth/login - Failure (401 Unauthorized) for denied user")
    void login_deniedUser_shouldReturnUnauthorized() throws Exception {
        final LoginRequestDto requestDto = new LoginRequestDto(DENIED_DNI);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.error", is("Unauthorized")))
                .andExpect(jsonPath("$.message",
                        containsString("User account status issue")
                ))
                .andExpect(jsonPath("$.message",
                        containsString("User account with DNI " + DENIED_DNI
                                + " is denied access."
                        )
                ));
    }

    @Test
    @DisplayName("POST /auth/login - Failure (401 Unauthorized) for non-existent user")
    void login_notFoundUser_shouldReturnUnauthorized() throws Exception {
        final LoginRequestDto requestDto = new LoginRequestDto(NOT_FOUND_DNI);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.error", is("Unauthorized")))
                .andExpect(jsonPath("$.message",
                        is("Invalid credentials provided.")
                ));
    }

    @Test
    @DisplayName("POST /auth/login - Failure (400 Bad Request) for invalid DNI")
    void login_invalidDni_shouldReturnBadRequest() throws Exception {
        final LoginRequestDto requestDto = new LoginRequestDto(INVALID_DNI);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Validation Error")))
                .andExpect(jsonPath("$.message",
                        containsString("DNI must contain only digits")
                ));
    }
}