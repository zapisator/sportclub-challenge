package com.sportclub.challenge.application.service;

import com.sportclub.challenge.application.port.out.log.LoggingPort;
import com.sportclub.challenge.application.port.out.persistence.target.TargetUserRepositoryPort;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Tests for UserPaginationService")
class UserPaginationServiceTest {

    @Mock
    private TargetUserRepositoryPort targetUserRepositoryPort;

    @Mock
    private LoggingPort logger;

    @InjectMocks
    private UserPaginationService userPaginationService;

    private Pageable defaultPageable;
    private Branch testBranch;
    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        defaultPageable = PageRequest.of(0, 10, Sort.by("firstName").ascending());
        testBranch = new Branch("B001", "Test Branch", "123 Test St", "Test City");
        user1 = new User(
                "U001", "Alice", "Smith", "a@a.com",
                "111", "1111", UserState.AUTHORIZED, testBranch
        );
        user2 = new User(
                "U002", "Bob", "Jones", "b@b.com",
                "222", "2222", UserState.AUTHORIZED, testBranch
        );
    }

    @Test
    @DisplayName("Should call repository findAll and return Page of Users")
    void shouldCallRepositoryAndReturnPage() {
        final List<User> userList = Arrays.asList(user1, user2);
        final Page<User> expectedPage = new PageImpl<>(userList, defaultPageable, userList.size());
        when(targetUserRepositoryPort.findAll(any(Pageable.class))).thenReturn(expectedPage);

        final Page<User> actualPage = userPaginationService.findUsers(defaultPageable);

        assertThat(actualPage).isEqualTo(expectedPage);
        verify(targetUserRepositoryPort, times(1)).findAll(defaultPageable);
        verify(logger, times(1)).info(
                "Fetching users with pagination: page={}, size={}, sort={}",
                0, 10, Sort.by("firstName").ascending()
        );
        verify(logger, times(1)).info(
                "Found {} users on page {}/{} (total elements: {})",
                2, 0, 1, 2L
        );
    }

    @Test
    @DisplayName("Should return empty Page when repository returns empty Page")
    void shouldReturnEmptyPageWhenRepositoryReturnsEmpty() {
        final Page<User> emptyPage = new PageImpl<>(Collections.emptyList(), defaultPageable, 0);
        when(targetUserRepositoryPort.findAll(any(Pageable.class))).thenReturn(emptyPage);

        final Page<User> actualPage = userPaginationService.findUsers(defaultPageable);

        assertThat(actualPage).isNotNull();
        assertThat(actualPage.getContent()).isEmpty();
        assertThat(actualPage.getTotalElements()).isZero();
        assertThat(actualPage.getTotalPages()).isZero();
        assertThat(actualPage.getNumber()).isZero();
        verify(targetUserRepositoryPort, times(1)).findAll(defaultPageable);
        verify(logger, times(1)).info(
                "Fetching users with pagination: page={}, size={}, sort={}",
                0, 10, Sort.by("firstName").ascending()
        );
        verify(logger, times(1)).info(
                "Found {} users on page {}/{} (total elements: {})",
                0, 0, 0, 0L
        );
    }
}