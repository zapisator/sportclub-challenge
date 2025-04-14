package com.sportclub.challenge.adapter.out.persistence.target;

import com.sportclub.challenge.adapter.out.persistence.target.entity.TargetBranchJpaEntity;
import com.sportclub.challenge.adapter.out.persistence.target.mapper.TargetBranchPersistenceMapper;
import com.sportclub.challenge.adapter.out.persistence.target.repository.TargetBranchJpaRepository;
import com.sportclub.challenge.application.port.out.persistence.target.TargetBranchRepositoryPort;
import com.sportclub.challenge.domain.model.branch.Branch;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;

@Repository
@RequiredArgsConstructor
public class TargetBranchRepositoryAdapter implements TargetBranchRepositoryPort {

    private final TargetBranchJpaRepository branchJpaRepository;
    private final TargetBranchPersistenceMapper branchMapper;

    @Override
    @Transactional("targetTransactionManager")
    public Branch save(Branch branch) {
        TargetBranchJpaEntity entityToSave = branchMapper.toEntity(branch);
        TargetBranchJpaEntity savedEntity = branchJpaRepository.save(entityToSave);
        return branchMapper.toDomain(savedEntity);
    }

    @Override
    @Transactional(value = "targetTransactionManager", readOnly = true)
    @Cacheable(value = "branchesById", key = "#id", unless = "#result == null || #result == T(java.util.Optional).empty()")
    public Optional<Branch> findById(String id) {
        return branchJpaRepository.findById(id)
                .map(branchMapper::toDomain);
    }

    @Override
    @Transactional(value = "targetTransactionManager", readOnly = true)
    public List<Branch> findAll() {
        return branchJpaRepository.findAll()
                .stream()
                .map(branchMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(value = "targetTransactionManager", readOnly = true)
    public Map<String, Branch> findAllAsMap() {
        return  branchJpaRepository.findAll()
                .stream()
                .map(branchMapper::toDomain)
                .collect(Collectors.toMap(Branch::id, identity()));
    }

}
