package com.jeka8833.tntclientwebendpoints.web.services.web;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

@Service
public final class UserService {
    public Optional<UUID> getUser(Authentication authentication) {
        try {
            return Optional.of(UUID.fromString(authentication.getName()));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public UUID getUserOrThrow(Authentication authentication) throws ResponseStatusException {
        return getUser(authentication).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a player"));
    }
}
