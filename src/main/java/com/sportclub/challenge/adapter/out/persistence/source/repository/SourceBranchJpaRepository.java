package com.sportclub.challenge.adapter.out.persistence.source.repository;

import com.sportclub.challenge.adapter.out.persistence.source.entity.SourceBranchJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SourceBranchJpaRepository extends JpaRepository<SourceBranchJpaEntity, String> {
}
