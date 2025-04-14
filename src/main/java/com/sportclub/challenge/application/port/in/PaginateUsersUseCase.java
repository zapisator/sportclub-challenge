package com.sportclub.challenge.application.port.in;

import com.sportclub.challenge.domain.model.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaginateUsersUseCase {
    Page<User> findUsers(Pageable pageable);
}
