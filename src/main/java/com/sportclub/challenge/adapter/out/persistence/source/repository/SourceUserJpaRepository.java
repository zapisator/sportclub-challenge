package com.sportclub.challenge.adapter.out.persistence.source.repository;

import com.sportclub.challenge.adapter.out.persistence.source.entity.SourceUserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SourceUserJpaRepository extends JpaRepository<SourceUserJpaEntity, String> {
}
