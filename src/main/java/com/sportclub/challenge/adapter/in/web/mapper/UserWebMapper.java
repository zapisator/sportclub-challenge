package com.sportclub.challenge.adapter.in.web.mapper;

import com.sportclub.challenge.adapter.in.web.dto.request.LoginRequestDto;
import com.sportclub.challenge.adapter.in.web.dto.response.UserDto;
import com.sportclub.challenge.application.port.in.command.LoginCommand;
import com.sportclub.challenge.domain.model.user.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = BranchWebMapper.class)
public interface UserWebMapper {

    UserDto domainToDto(User user);

    List<UserDto> domainListToDtoList(List<User> users);

    LoginCommand requestToCommand(LoginRequestDto dto);
}
