package com.jeka8833.tntclientendpoints.services.restapi.controllers.web;

import com.jeka8833.tntclientendpoints.services.restapi.services.web.UserService;
import com.jeka8833.tntclientendpoints.services.restapi.services.web.security.TempSecurityTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Controller;

import java.util.Optional;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class LogoutController implements LogoutSuccessHandler {
    private final UserService userService;
    private final TempSecurityTokenService tempSecurityTokenService;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
                                Authentication authentication) {
        Optional<UUID> player = userService.getUser(authentication);

        player.ifPresent(tempSecurityTokenService::invalidate);
    }
}
