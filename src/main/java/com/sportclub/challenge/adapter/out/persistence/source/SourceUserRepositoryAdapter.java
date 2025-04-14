package com.sportclub.challenge.adapter.out.persistence.source;

import com.sportclub.challenge.adapter.out.persistence.source.mapper.SourceUserPersistenceMapper;
import com.sportclub.challenge.adapter.out.persistence.source.repository.SourceUserJpaRepository;
import com.sportclub.challenge.application.port.out.persistence.source.SourceUserRepositoryPort;
import com.sportclub.challenge.domain.model.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class SourceUserRepositoryAdapter implements SourceUserRepositoryPort {
    private final SourceUserJpaRepository sourceUserJpaRepository;
    private final SourceUserPersistenceMapper sourceUserMapper;

    @Override
    @Transactional(value = "sourceTransactionManager", readOnly = true)
    public List<User> findAllUsersFromSource() {
        return sourceUserJpaRepository.findAll()
                .stream()
                .map(sourceUserMapper::toDomain)
                .toList();
    }
}
