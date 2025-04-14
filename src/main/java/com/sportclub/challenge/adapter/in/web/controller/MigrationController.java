package com.sportclub.challenge.adapter.in.web.controller;

import com.sportclub.challenge.adapter.in.web.dto.response.ErrorResponseDto;
import com.sportclub.challenge.adapter.in.web.dto.response.SuccessMessageDto;
import com.sportclub.challenge.application.port.in.MigrateDataUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/migrate")
@RequiredArgsConstructor
@Tag(name = "Data Migration", description = "Endpoints for triggering data migration between databases")
@SecurityRequirement(name = "bearerAuth")
public class MigrationController {

    private final MigrateDataUseCase migrateDataUseCase;

    @Operation(summary = "Trigger Data Migration",
            description = "Starts the process of migrating branches and users from the source database to the target database. Requires ADMIN role.")
    @ApiResponse(responseCode = "200", description = "Migration process completed successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = SuccessMessageDto.class)))
    @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponseDto.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden - User does not have ADMIN role",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponseDto.class)))
    @ApiResponse(responseCode = "500", description = "Migration failed due to internal server error",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponseDto.class)))
    @PostMapping
    public ResponseEntity<SuccessMessageDto> triggerMigration() {
        migrateDataUseCase.migrateData();
        return ResponseEntity.ok(new SuccessMessageDto(
                "Data migration process triggered and completed."
        ));
    }
}
