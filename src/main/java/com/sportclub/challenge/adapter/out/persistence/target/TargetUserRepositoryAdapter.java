package com.sportclub.challenge.adapter.out.persistence.target;

import com.sportclub.challenge.adapter.out.persistence.target.entity.TargetUserJpaEntity;
import com.sportclub.challenge.adapter.out.persistence.target.mapper.TargetUserPersistenceMapper;
import com.sportclub.challenge.adapter.out.persistence.target.repository.TargetUserJpaRepository;
import com.sportclub.challenge.application.port.out.persistence.target.TargetUserRepositoryPort;
import com.sportclub.challenge.domain.model.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;

@Repository
@RequiredArgsConstructor
public class TargetUserRepositoryAdapter implements TargetUserRepositoryPort {

    private final TargetUserJpaRepository userJpaRepository;
    private final TargetUserPersistenceMapper userMapper;

    @Override
    @Transactional("targetTransactionManager")
    public User save(User user) {
        TargetUserJpaEntity entityToSave = userMapper.toEntity(user);
        TargetUserJpaEntity savedEntity = userJpaRepository.save(entityToSave);
        return userMapper.toDomain(savedEntity);
    }

    @Override
    @Transactional(value = "targetTransactionManager", readOnly = true)
    @Cacheable(value = "usersById", key = "#id", unless = "#result == null || #result == T(java.util.Optional).empty()")
    public Optional<User> findById(String id) {
        return userJpaRepository.findById(id)
                .map(userMapper::toDomain);
    }

    @Override
    @Transactional(value = "targetTransactionManager", readOnly = true)
    @Cacheable(value = "usersByDni", key = "#dni", unless = "#result == null || #result == T(java.util.Optional).empty()")
        public Optional<User> findByDni(String dni) {
        return userJpaRepository.findByDni(dni)
                .map(userMapper::toDomain);
    }

    @Override
    @Transactional(value = "targetTransactionManager", readOnly = true)
    public Page<User> findAll(Pageable pageable) {
        Page<TargetUserJpaEntity> entityPage = userJpaRepository.findAll(pageable);
        return entityPage.map(userMapper::toDomain);
    }

    @Override
    @Transactional(value = "targetTransactionManager", readOnly = true)
    public List<User> findAll() {
        return userJpaRepository.findAll()
                .stream()
                .map(userMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(value = "targetTransactionManager", readOnly = true)
    public Map<String, User> findAllAsMap() {
        return userJpaRepository.findAll()
                .stream()
                .map(userMapper::toDomain)
                .collect(Collectors.toMap(User::id, identity()));
    }
}
