package com.sportclub.challenge.adapter.out.persistence.source.mapper;

import com.sportclub.challenge.adapter.out.persistence.source.entity.SourceBranchJpaEntity;
import com.sportclub.challenge.domain.model.branch.Branch;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(
        componentModel = SPRING,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface SourceBranchPersistenceMapper {

    Branch toDomain(SourceBranchJpaEntity entity);

    @Mapping(target = "users", ignore = true)
    SourceBranchJpaEntity toEntity(Branch domain);
}
