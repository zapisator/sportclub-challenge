package com.sportclub.challenge.adapter.out.security.service;

import com.sportclub.challenge.application.port.out.log.LoggingPort;
import com.sportclub.challenge.application.port.out.persistence.target.TargetUserRepositoryPort;
import com.sportclub.challenge.domain.model.user.User;
import com.sportclub.challenge.domain.model.user.UserState;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final TargetUserRepositoryPort userRepositoryPort;
    private final LoggingPort logger;

    @Override
    @Transactional(value = "targetTransactionManager", readOnly = true)
    public UserDetails loadUserByUsername(String dni) throws UsernameNotFoundException {
        logger.debug("Attempting to load user by DNI (username): {}", dni);

        final User domainUser = userRepositoryPort.findByDni(dni)
                .orElseThrow(() -> {
                    String message = "User not found with DNI: " + dni;
                    logger.warn(message);
                    return new UsernameNotFoundException(message);
                });

        logger.debug("User found with DNI: {}. State: {}", dni, domainUser.state());

        final Collection<? extends GrantedAuthority> authorities = determineAuthorities(domainUser);
        boolean enabled = UserState.AUTHORIZED.equals(domainUser.state());
        boolean accountNonLocked = enabled;
        boolean credentialsNonExpired = true;
        boolean accountNonExpired = true;
        return new org.springframework.security.core.userdetails.User(
                domainUser.dni(),
                "[PROTECTED]",
                enabled,
                accountNonExpired,
                credentialsNonExpired,
                accountNonLocked,
                authorities
        );
    }

    private Collection<? extends GrantedAuthority> determineAuthorities(User user) {
        logger.debug("Assigning default 'ROLE_USER' authority to user with DNI: {}", user.dni());
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }
}
