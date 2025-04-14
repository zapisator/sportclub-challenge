package com.sportclub.challenge.adapter.in.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sportclub.challenge.adapter.in.web.dto.request.AccessRequestDto;
import com.sportclub.challenge.application.port.out.persistence.target.TargetBranchRepositoryPort;
import com.sportclub.challenge.application.port.out.persistence.target.TargetUserRepositoryPort;
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
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Integration Tests for AccessController (/acceso)")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AccessControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TargetUserRepositoryPort userRepositoryPort;

    @Autowired
    private TargetBranchRepositoryPort branchRepositoryPort;

    private static final String AUTHORIZED_DNI = "11111111";
    private static final String DENIED_DNI = "22222222";
    private static final String NOT_FOUND_DNI = "99999999";
    private static final String INVALID_FORMAT_DNI = "ABC";
    private static final String INVALID_LENGTH_DNI = "123";

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
    @DisplayName("POST /acceso - Success (200 OK) for authorized user")
    void validateAccess_authorizedUser_shouldReturnOk() throws Exception {
        final AccessRequestDto requestDto = new AccessRequestDto(AUTHORIZED_DNI);

        mockMvc.perform(post("/acceso")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", is("Acceso permitido")));
    }

    @Test
    @DisplayName("POST /acceso - Failure (403 Forbidden) for denied user")
    void validateAccess_deniedUser_shouldReturnForbidden() throws Exception {
        final AccessRequestDto requestDto = new AccessRequestDto(DENIED_DNI);

        mockMvc.perform(post("/acceso")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(403)))
                .andExpect(jsonPath("$.error", is("Forbidden")))
                .andExpect(jsonPath("$.message", is("Access denied for user with DNI "
                        + DENIED_DNI + ". User state is DENIED."
                )));
    }

    @Test
    @DisplayName("POST /acceso - Failure (404 Not Found) for non-existent user")
    void validateAccess_notFoundUser_shouldReturnNotFound() throws Exception {
        final AccessRequestDto requestDto = new AccessRequestDto(NOT_FOUND_DNI);

        mockMvc.perform(post("/acceso")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("User Not Found")))
                .andExpect(jsonPath("$.message", is("User with DNI " + NOT_FOUND_DNI + " not found.")));
    }

    @Test
    @DisplayName("POST /acceso - Failure (400 Bad Request) for invalid DNI format")
    void validateAccess_invalidFormatDni_shouldReturnBadRequest() throws Exception {
        final AccessRequestDto requestDto = new AccessRequestDto(INVALID_FORMAT_DNI);

        mockMvc.perform(post("/acceso")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Validation Error")))
                .andExpect(jsonPath("$.message", containsString("DNI must contain only digits")));
    }

    @Test
    @DisplayName("POST /acceso - Failure (400 Bad Request) for invalid DNI length")
    void validateAccess_invalidLengthDni_shouldReturnBadRequest() throws Exception {
        AccessRequestDto requestDto = new AccessRequestDto(INVALID_LENGTH_DNI);

        mockMvc.perform(post("/acceso")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Validation Error")))
                .andExpect(jsonPath("$.message",
                        containsString("DNI must be between 7 and 9 digits")
                ));
    }

    @Test
    @DisplayName("POST /acceso - Failure (400 Bad Request) for blank DNI")
    void validateAccess_blankDni_shouldReturnBadRequest() throws Exception {
        String jsonRequest = """
                 {
                   "dni": ""
                 }
                """;


        mockMvc.perform(post("/acceso")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Validation Error")))
                .andExpect(jsonPath("$.message",
                        containsString("DNI cannot be blank")
                ))
                .andExpect(jsonPath("$.message",
                        containsString("DNI must be between 7 and 9 digits")
                ));
    }
}