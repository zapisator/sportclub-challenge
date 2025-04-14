package com.sportclub.challenge.application.port.out.persistence.source;

import com.sportclub.challenge.domain.model.branch.Branch;

import java.util.List;

public interface SourceBranchRepositoryPort {
    List<Branch> findAllBranchesFromSource();
}
