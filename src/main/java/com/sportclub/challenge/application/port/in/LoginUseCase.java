package com.sportclub.challenge.application.port.in;

import com.sportclub.challenge.application.port.in.command.LoginCommand;

public interface LoginUseCase {
    String login(LoginCommand command);
}
