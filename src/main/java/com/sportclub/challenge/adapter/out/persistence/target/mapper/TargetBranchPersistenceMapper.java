package com.sportclub.challenge.adapter.out.persistence.target.mapper;

import com.sportclub.challenge.adapter.out.persistence.target.entity.TargetBranchJpaEntity;
import com.sportclub.challenge.domain.model.branch.Branch;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(
        componentModel = SPRING,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface TargetBranchPersistenceMapper {

    Branch toDomain(TargetBranchJpaEntity entity);

    @Mapping(target = "users", ignore = true)
    TargetBranchJpaEntity toEntity(Branch domain);
}
