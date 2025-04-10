package com.sportclub.challenge.domain.port.repository;

import com.sportclub.challenge.domain.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface UserRepositoryPort {

    User save(User user);

    Optional<User> findById(String id);

    Optional<User> findByDni(String dni);

    Page<User> findAll(Pageable pageable);

    List<User> findAll();

    Map<String, User> findAllAsMap();
}
