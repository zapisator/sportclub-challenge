package com.sportclub.challenge.application.port.out.security;

import java.util.Optional;

public interface JwtProviderPort {

    String generateToken(String dni);

    boolean validateToken(String token);

    Optional<String> getDniFromToken(String token);

}
