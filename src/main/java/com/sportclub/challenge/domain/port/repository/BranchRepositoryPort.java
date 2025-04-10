package com.sportclub.challenge.domain.port.repository;

import com.sportclub.challenge.domain.model.Branch;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface BranchRepositoryPort {
    Branch save(Branch branch);

    Optional<Branch> findById(String id);

    List<Branch> findAll();

    Map<String, Branch> findAllAsMap();
}
