package com.sportclub.challenge.adapter.out.persistence.source.mapper;

import com.sportclub.challenge.adapter.out.persistence.source.entity.SourceUserJpaEntity;
import com.sportclub.challenge.domain.model.user.User;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(
        componentModel = SPRING,
        uses = {SourceBranchPersistenceMapper.class},
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface SourceUserPersistenceMapper {

    User toDomain(SourceUserJpaEntity entity);

    SourceUserJpaEntity toEntity(User domain);
}