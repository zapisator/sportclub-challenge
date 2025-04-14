package com.sportclub.challenge.adapter.in.web.controller;

import com.sportclub.challenge.adapter.in.web.dto.response.ErrorResponseDto;
import com.sportclub.challenge.adapter.in.web.dto.response.PageResultDto;
import com.sportclub.challenge.adapter.in.web.dto.response.UserDto;
import com.sportclub.challenge.adapter.in.web.mapper.UserWebMapper;
import com.sportclub.challenge.application.port.in.PaginateUsersUseCase;
import com.sportclub.challenge.domain.model.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Endpoints for managing users")
public class UserController {

    private final PaginateUsersUseCase paginateUsersUseCase;
    private final UserWebMapper userWebMapper;

    @Operation(summary = "Get paginated list of users",
            description = "Retrieves a list of users with pagination and optional sorting. " +
                    "Sorting can be done by fields like 'firstName', 'lastName', 'email'. Example: `sort=lastName,asc`")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list of users",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = PageResultDto.class)))
    @ApiResponse(responseCode = "400", description = "Invalid pagination or sorting parameters",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponseDto.class)))
    @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponseDto.class)))
    @GetMapping
    public ResponseEntity<PageResultDto<UserDto>> getPaginatedUsers(
            @ParameterObject @PageableDefault(size = 10) Pageable pageable
    ) {
        final Page<User> userPage = paginateUsersUseCase.findUsers(pageable);
        final List<UserDto> userDtos = userWebMapper.domainListToDtoList(userPage.getContent());
        final PageResultDto<UserDto> pageResultDto = new PageResultDto<>(
                userPage.getNumber(),
                userPage.getTotalPages(),
                userPage.getTotalElements(),
                userPage.getSize(),
                userDtos
        );
        return ResponseEntity.ok(pageResultDto);
    }
}
