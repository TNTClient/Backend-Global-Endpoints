package com.jeka8833.tntclientendpoints.services.restapi.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
final class GeneralController {

    @GetMapping("api/v1/user/roles")
    private Set<String> getRoles(Authentication authentication) {
        return AuthorityUtils.authorityListToSet(authentication.getAuthorities());
    }
}
