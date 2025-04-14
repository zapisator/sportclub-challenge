package com.sportclub.challenge.adapter.out.persistence.target.repository;

import com.sportclub.challenge.adapter.out.persistence.target.entity.TargetBranchJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TargetBranchJpaRepository extends JpaRepository<TargetBranchJpaEntity, String> {
}
