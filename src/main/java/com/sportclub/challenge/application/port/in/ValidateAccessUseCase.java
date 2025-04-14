package com.sportclub.challenge.application.port.in;

import com.sportclub.challenge.domain.model.user.User;

public interface ValidateAccessUseCase {
    User validateAccessByDni(String dni);
}
