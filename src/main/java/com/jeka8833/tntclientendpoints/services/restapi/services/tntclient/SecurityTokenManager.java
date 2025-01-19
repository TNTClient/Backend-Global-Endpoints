package com.jeka8833.tntclientendpoints.services.restapi.services.tntclient;

import com.jeka8833.tntclientendpoints.services.general.tntclient.websocket.TNTClientApi;
import com.jeka8833.tntclientendpoints.services.general.tntclient.websocket.packet.serverbound.ServerboundWebToken;
import com.jeka8833.tntclientendpoints.services.restapi.models.SecurityUser;
import com.jeka8833.tntclientendpoints.services.restapi.services.web.security.TempSecurityTokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
public class SecurityTokenManager {
    private final TNTClientApi tntClientApi;
    private final TempSecurityTokenService tempSecurityTokenService;
    private final long tempTokenNoUseExpireNanos;

    public SecurityTokenManager(TNTClientApi tntClientApi, TempSecurityTokenService tempSecurityTokenService,
                                @Value("${web.security.temptoken.expire.no-use:5m}") Duration tempTokenNoUseExpire) {
        this.tntClientApi = tntClientApi;
        this.tempSecurityTokenService = tempSecurityTokenService;
        this.tempTokenNoUseExpireNanos = tempTokenNoUseExpire.toNanos();
    }

    public boolean generateAndSendToken(UUID player, boolean register) {
        if (register) {
            SecurityUser securityUser = tempSecurityTokenService.create(player, tempTokenNoUseExpireNanos);

            return tntClientApi.send(new ServerboundWebToken(player, securityUser.getPasswordUUID()));
        } else {
            tempSecurityTokenService.invalidate(player);

            return tntClientApi.send(new ServerboundWebToken(player));
        }
    }
}
