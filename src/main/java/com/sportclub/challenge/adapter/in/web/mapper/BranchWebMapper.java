package com.sportclub.challenge.adapter.in.web.mapper;


import com.sportclub.challenge.adapter.in.web.dto.response.BranchDto;
import com.sportclub.challenge.domain.model.branch.Branch;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface BranchWebMapper {

    BranchDto domainToDto(Branch branch);

}
