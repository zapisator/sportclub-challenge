package com.sportclub.challenge.adapter.out.persistence.target.repository;

import com.sportclub.challenge.adapter.out.persistence.target.entity.TargetUserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TargetUserJpaRepository extends JpaRepository<TargetUserJpaEntity, String> {
    Optional<TargetUserJpaEntity> findByDni(String dni);

}
