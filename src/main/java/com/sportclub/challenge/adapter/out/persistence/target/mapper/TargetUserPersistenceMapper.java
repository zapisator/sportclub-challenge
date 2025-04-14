package com.sportclub.challenge.adapter.out.persistence.target.mapper;

import com.sportclub.challenge.adapter.out.persistence.target.entity.TargetUserJpaEntity;
import com.sportclub.challenge.domain.model.user.User;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(
        componentModel = SPRING,
        uses = {TargetBranchPersistenceMapper.class},
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface TargetUserPersistenceMapper {

    User toDomain(TargetUserJpaEntity entity);

    TargetUserJpaEntity toEntity(User domain);

}
