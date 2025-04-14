package com.sportclub.challenge.application.service;

import com.sportclub.challenge.application.port.in.PaginateUsersUseCase;
import com.sportclub.challenge.application.port.out.log.LoggingPort;
import com.sportclub.challenge.application.port.out.persistence.target.TargetUserRepositoryPort;
import com.sportclub.challenge.domain.model.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserPaginationService implements PaginateUsersUseCase {

    private final TargetUserRepositoryPort targetUserRepositoryPort;
    private final LoggingPort logger;

    @Override
    @Transactional(value = "targetTransactionManager", readOnly = true)
    public Page<User> findUsers(Pageable pageable) {
        logger.info("Fetching users with pagination: page={}, size={}, sort={}",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        final Page<User> userPage = targetUserRepositoryPort.findAll(pageable);
        logger.info("Found {} users on page {}/{} (total elements: {})",
                userPage.getNumberOfElements(),
                userPage.getNumber(),
                userPage.getTotalPages(),
                userPage.getTotalElements());
        return userPage;
    }
}
