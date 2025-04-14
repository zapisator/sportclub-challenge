package com.sportclub.challenge.application.port.out.persistence.source;

import com.sportclub.challenge.domain.model.user.User;

import java.util.List;

public interface SourceUserRepositoryPort {
    List<User> findAllUsersFromSource();
}
