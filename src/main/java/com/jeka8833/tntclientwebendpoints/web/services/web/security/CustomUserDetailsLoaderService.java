package com.jeka8833.tntclientwebendpoints.web.services.web.security;

import com.jeka8833.tntclientwebendpoints.models.TNTClientUser;
import com.jeka8833.tntclientwebendpoints.repositories.UserRepository;
import com.jeka8833.tntclientwebendpoints.web.model.SecurityUser;
import com.jeka8833.tntclientwebendpoints.web.services.web.UserService;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class CustomUserDetailsLoaderService implements UserDetailsService {
    private final TempSecurityTokenService tempSecurityTokenService;
    private final UserRepository userRepository;
    private final UserService userService;

    public CustomUserDetailsLoaderService(TempSecurityTokenService tempSecurityTokenService,
                                          UserRepository userRepository, UserService userService) {
        this.tempSecurityTokenService = tempSecurityTokenService;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent success) {
        Optional<UUID> userUuidOpt = userService.getUser(success.getAuthentication());
        if (userUuidOpt.isEmpty()) return;


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

            securityUser.setAuthorities(databaseUser.getRoles());

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
