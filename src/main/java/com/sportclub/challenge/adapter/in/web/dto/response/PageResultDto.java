package com.sportclub.challenge.adapter.in.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Paginated result wrapper")
public record PageResultDto<T>(
        @Schema(description = "Current page number (0-based)", example = "0")
        int currentPage,

        @Schema(description = "Total number of pages available", example = "10")
        int totalPages,

        @Schema(description = "Total number of items across all pages", example = "100")
        long totalItems,

        @Schema(description = "Number of items requested per page", example = "10")
        int pageSize,

        @Schema(description = "List of items on the current page")
        List<T> content
) {}
