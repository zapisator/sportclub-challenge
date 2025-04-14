package com.sportclub.challenge.adapter.in.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Branch Information")
public record BranchDto(
        @Schema(description = "Unique identifier of the branch", example = "B001")
        String id,

        @Schema(description = "Name of the branch", example = "Sede Central")
        String name,

        @Schema(description = "Address of the branch", example = "Calle 123")
        String address,

        @Schema(description = "City where the branch is located", example = "Buenos Aires")
        String city
) {}
