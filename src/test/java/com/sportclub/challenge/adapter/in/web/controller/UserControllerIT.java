package com.sportclub.challenge.adapter.in.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sportclub.challenge.adapter.out.persistence.target.repository.TargetBranchJpaRepository;
import com.sportclub.challenge.adapter.out.persistence.target.repository.TargetUserJpaRepository;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Integration Tests for UserController (/usuarios)")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserControllerIT {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private TargetUserRepositoryPort userRepositoryPort;
    @Autowired
    private TargetBranchRepositoryPort branchRepositoryPort;
    @Autowired
    private TargetUserJpaRepository targetUserJpaRepository;
    @Autowired
    private TargetBranchJpaRepository targetBranchJpaRepository;
    @Autowired
    private JwtProviderPort jwtProviderPort;

    private String validToken;
    private final String AUTHORIZED_DNI_FOR_TOKEN = "11111111";

    @BeforeEach
    void setUpDatabaseAndToken() {
        targetUserJpaRepository.deleteAllInBatch();
        targetBranchJpaRepository.deleteAllInBatch();
        final Branch branch = branchRepositoryPort.save(new Branch("B1", "IT Branch", "Tech Street", "Dev City"));
        userRepositoryPort.save(User.builder()
                .id("U1")
                .firstName("Auth")
                .lastName("User")
                .email("auth@it.com")
                .phone("111")
                .dni(AUTHORIZED_DNI_FOR_TOKEN)
                .state(UserState.AUTHORIZED)
                .branch(branch)
                .build());
        userRepositoryPort.save(User.builder()
                .id("U2")
                .firstName("Charlie")
                .lastName("Brown")
                .email("c@b.com")
                .phone("222")
                .dni("22222222")
                .state(UserState.AUTHORIZED)
                .branch(branch)
                .build());
        userRepositoryPort.save(User.builder()
                .id("U3")
                .firstName("Alice")
                .lastName("Smith")
                .email("a@s.com")
                .phone("333")
                .dni("33333333")
                .state(UserState.DENIED)
                .branch(branch)
                .build());
        userRepositoryPort.save(User.builder()
                .id("U4")
                .firstName("Bob")
                .lastName("Johnson")
                .email("b@j.com")
                .phone("444")
                .dni("44444444")
                .state(UserState.AUTHORIZED)
                .branch(branch)
                .build());
        validToken = jwtProviderPort.generateToken(AUTHORIZED_DNI_FOR_TOKEN);
    }

    @Test
    @DisplayName("GET /usuarios - Success (200 OK) with valid token, returns paginated users")
    void getUsers_withValidToken_shouldReturnPaginatedUsers() throws Exception {
        mockMvc.perform(get("/usuarios")
                        .param("page", "0")
                        .param("size", "2")
                        .param("sort", "firstName,asc")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.currentPage", is(0)))
                .andExpect(jsonPath("$.pageSize", is(2)))
                .andExpect(jsonPath("$.totalItems", is(4)))
                .andExpect(jsonPath("$.totalPages", is(2)))
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].firstName", is("Alice")))
                .andExpect(jsonPath("$.content[0].dni", is("33333333")))
                .andExpect(jsonPath("$.content[0].state", is("DENIED")))
                .andExpect(jsonPath("$.content[1].firstName", is("Auth")))
                .andExpect(jsonPath("$.content[1].dni", is(AUTHORIZED_DNI_FOR_TOKEN)));
    }

    @Test
    @DisplayName("GET /usuarios - Success (200 OK) with valid token, test second page and sorting")
    void getUsers_withValidToken_shouldReturnSecondPageSorted() throws Exception {
        mockMvc.perform(get("/usuarios")
                        .param("page", "1")
                        .param("size", "2")
                        .param("sort", "lastName,desc")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentPage", is(1)))
                .andExpect(jsonPath("$.pageSize", is(2)))
                .andExpect(jsonPath("$.totalItems", is(4)))
                .andExpect(jsonPath("$.totalPages", is(2)))
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].lastName", is("Johnson")))
                .andExpect(jsonPath("$.content[1].lastName", is("Brown")));
    }

    @Test
    @DisplayName("GET /usuarios - Failure (401 Unauthorized) without token")
    void getUsers_withoutToken_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/usuarios")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.error", is("Unauthorized")))
                .andExpect(jsonPath("$.message",
                        containsString("Authentication required")
                ));
    }

    @Test
    @DisplayName("GET /usuarios - Failure (401 Unauthorized) with invalid token")
    void getUsers_withInvalidToken_shouldReturnUnauthorized() throws Exception {
        final String invalidToken = "Bearer invalid.token.string";
        mockMvc.perform(get("/usuarios")
                        .param("page", "0")
                        .param("size", "10")
                        .header(HttpHeaders.AUTHORIZATION, invalidToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.error", is("Unauthorized")));
    }

    @Test
    @DisplayName("GET /usuarios - Failure (401 Unauthorized) with expired token")
    void getUsers_withExpiredToken_shouldReturnUnauthorized() throws Exception {
        final String expiredToken = "Bearer expired.jwt.token";
        mockMvc.perform(get("/usuarios")
                        .header(HttpHeaders.AUTHORIZATION, expiredToken))
                .andExpect(status().isUnauthorized());
    }
}