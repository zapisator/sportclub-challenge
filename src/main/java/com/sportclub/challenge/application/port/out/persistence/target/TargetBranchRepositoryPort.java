package com.sportclub.challenge.application.port.out.persistence.target;

import com.sportclub.challenge.domain.model.branch.Branch;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface TargetBranchRepositoryPort {
    Branch save(Branch branch);

    Optional<Branch> findById(String id);

    List<Branch> findAll();

    Map<String, Branch> findAllAsMap();
}
