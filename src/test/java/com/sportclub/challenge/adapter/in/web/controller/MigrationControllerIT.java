package com.sportclub.challenge.adapter.in.web.controller;

import com.sportclub.challenge.application.exception.MigrationFailedException;
import com.sportclub.challenge.application.port.in.MigrateDataUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Integration Tests for MigrationController (Web Layer with @TestConfiguration)")
class MigrationControllerIT {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private MigrateDataUseCase migrateDataUseCase;
    private static final String MIGRATE_ENDPOINT = "/migrate";

    @TestConfiguration
    static class TestConfig {

        @Bean
        @Primary
        public MigrateDataUseCase mockMigrateDataUseCase() {
            return Mockito.mock(MigrateDataUseCase.class);
        }

    }

    @Test
    @DisplayName("POST /migrate - Failure (500 Internal Server Error) when use case throws exception")
    @WithMockUser(roles = "ADMIN")
    void triggerMigration_useCaseThrowsException_shouldReturnInternalServerError() throws Exception {
        doThrow(new MigrationFailedException("Test job launch failure", new RuntimeException()))
                .when(migrateDataUseCase).migrateData();

        mockMvc.perform(post(MIGRATE_ENDPOINT)
                        .with(csrf()))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", containsString("Migration Error")))
                .andExpect(jsonPath("$.message", containsString("Test job launch failure")));
        verify(migrateDataUseCase, times(1)).migrateData();
    }
}