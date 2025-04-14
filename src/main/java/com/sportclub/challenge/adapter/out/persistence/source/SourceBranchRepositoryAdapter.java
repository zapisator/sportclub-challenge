package com.sportclub.challenge.adapter.out.persistence.source;

import com.sportclub.challenge.adapter.out.persistence.source.mapper.SourceBranchPersistenceMapper;
import com.sportclub.challenge.adapter.out.persistence.source.repository.SourceBranchJpaRepository;
import com.sportclub.challenge.application.port.out.persistence.source.SourceBranchRepositoryPort;
import com.sportclub.challenge.domain.model.branch.Branch;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class SourceBranchRepositoryAdapter implements SourceBranchRepositoryPort {
    private final SourceBranchJpaRepository sourceBranchJpaRepository;
    private final SourceBranchPersistenceMapper sourceBranchMapper;

    @Override
    @Transactional(value = "sourceTransactionManager", readOnly = true)
    public List<Branch> findAllBranchesFromSource() {
        return sourceBranchJpaRepository.findAll()
                .stream()
                .map(sourceBranchMapper::toDomain)
                .toList();
    }
}
