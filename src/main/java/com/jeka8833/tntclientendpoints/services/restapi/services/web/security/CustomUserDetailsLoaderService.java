package com.jeka8833.tntclientendpoints.services.restapi.services.web.security;

import com.jeka8833.tntclientendpoints.services.restapi.models.SecurityUser;
import com.jeka8833.tntclientendpoints.services.restapi.models.TNTClientUser;
import com.jeka8833.tntclientendpoints.services.restapi.repositories.UserRepository;
import com.jeka8833.tntclientendpoints.services.restapi.services.web.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Service
public class CustomUserDetailsLoaderService implements UserDetailsService {
    private final TempSecurityTokenService tempSecurityTokenService;
    private final UserRepository userRepository;
    private final UserService userService;
    private final long tempTokenExpireTimeNanos;

    public CustomUserDetailsLoaderService(TempSecurityTokenService tempSecurityTokenService,
                                          UserRepository userRepository, UserService userService,
                                          @Value("${web.security.temptoken.expire}") Duration tempTokenExpireTime) {
        this.tempSecurityTokenService = tempSecurityTokenService;
        this.userRepository = userRepository;
        this.userService = userService;
        this.tempTokenExpireTimeNanos = tempTokenExpireTime.toNanos();

        if (tempTokenExpireTimeNanos <= 0) {
            throw new IllegalArgumentException("tempTokenExpireTime must be greater than 0");
        }
    }

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent success) {
        Optional<UUID> userUuidOpt = userService.getUser(success.getAuthentication());
        if (userUuidOpt.isEmpty()) return;

        SecurityUser securityUser = tempSecurityTokenService.get(userUuidOpt.get());
        if (securityUser == null) return;

        if (!securityUser.isLoginSuccess()) {
            securityUser.setExpireAtNanos(System.nanoTime() + tempTokenExpireTimeNanos);

            securityUser.setLoginSuccess(true);
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UUID userUUID = getUserUUID(username);

        SecurityUser securityUser = tempSecurityTokenService.get(userUUID);
        if (securityUser == null) {
            securityUser = new SecurityUser(userUUID);
        }

        Optional<TNTClientUser> databaseUserOpt = userRepository.findById(userUUID);
        if (databaseUserOpt.isPresent()) {
            TNTClientUser databaseUser = databaseUserOpt.get();

            if (databaseUser.getRoles() != null) {
                securityUser.setAuthorities(databaseUser.getRoles());
            }

            if (databaseUser.getStaticKey() != null) {
                securityUser.setPassword(databaseUser.getStaticKey());
            }
        }

        if (securityUser.getPassword() == null) throw new UsernameNotFoundException("User not found: " + username);

        return securityUser;
    }

    private static UUID getUserUUID(String username) {
        try {
            return UUID.fromString(username);
        } catch (Exception e) {
            throw new UsernameNotFoundException("User not found: " + username);
        }
    }
}
