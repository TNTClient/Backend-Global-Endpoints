package com.jeka8833.tntclientendpoints.services.restapi.services.web.security;

import com.jeka8833.tntclientendpoints.services.restapi.models.SecurityUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class TempSecurityTokenService {
    private final Map<UUID, SecurityUser> tempUsers = new ConcurrentHashMap<>();

    private final SessionRegistry sessionRegistry;

    @NotNull
    public SecurityUser create(@NotNull UUID user, long expireTimeNanos) {
        SecurityUser securityUser = new SecurityUser(user);
        securityUser.setPassword(UUID.randomUUID());
        securityUser.setExpireAtNanos(System.nanoTime() + expireTimeNanos);

        tempUsers.put(user, securityUser);

        return securityUser;
    }

    @Nullable
    public SecurityUser get(@Nullable UUID user) {
        if (user == null) return null;

        return tempUsers.get(user);
    }

    public void invalidate(@Nullable UUID user) {
        if (user == null) return;

        SecurityUser securityUser = tempUsers.remove(user);
        if (securityUser == null) return;

        securityUser.expireNow();

        List<SessionInformation> sessions = sessionRegistry.getAllSessions(securityUser, true);
        for (SessionInformation session : sessions) {
            session.expireNow();

            killExpiredSessionForSure(session.getSessionId());
        }
    }

    public void killExpiredSessionForSure(String id) {
        try {
            HttpHeaders requestHeaders = new HttpHeaders();
            requestHeaders.add("Cookie", "JSESSIONID=" + id);

            HttpEntity<?> requestEntity = new HttpEntity<>(null, requestHeaders);

            RestTemplate rt = new RestTemplate();
            rt.exchange("http://localhost:8080", HttpMethod.GET, requestEntity, String.class);
        } catch (Exception ex) {
            log.warn("Failed to kill expired session", ex);
        }
    }
}
