package com.sportclub.challenge.application.port.out.persistence.target;

import com.sportclub.challenge.domain.model.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface TargetUserRepositoryPort {

    User save(User user);

    Optional<User> findById(String id);

    Optional<User> findByDni(String dni);

    Page<User> findAll(Pageable pageable);

    List<User> findAll();

    Map<String, User> findAllAsMap();
}
