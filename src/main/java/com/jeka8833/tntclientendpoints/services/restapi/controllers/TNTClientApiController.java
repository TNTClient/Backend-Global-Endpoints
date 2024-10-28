package com.jeka8833.tntclientendpoints.services.restapi.controllers;

import com.jeka8833.tntclientendpoints.services.general.tntclintapi.TNTClientApi;
import com.jeka8833.tntclientendpoints.services.general.tntclintapi.packet.clientbound.ClientboundWebToken;
import com.jeka8833.tntclientendpoints.services.general.tntclintapi.packet.serverbound.ServerboundWebToken;
import com.jeka8833.tntclientendpoints.services.restapi.models.SecurityUser;
import com.jeka8833.tntclientendpoints.services.restapi.services.web.security.TempSecurityTokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;

@Component("web-tntClientApiController")
public class TNTClientApiController {

    public TNTClientApiController(TNTClientApi tntClientApi, TempSecurityTokenService tempSecurityTokenService,
                                  @Value("${web.security.temptoken.expire.no-use:5m}") Duration tempTokenNoUseExpire) {
        tntClientApi.registerListener(ClientboundWebToken.class, packet -> {
            UUID player = packet.getPlayer();

            if (packet.isRegister()) {
                SecurityUser securityUser = tempSecurityTokenService.create(player, tempTokenNoUseExpire.toNanos());

                tntClientApi.send(new ServerboundWebToken(player, securityUser.getPasswordUUID()));
            } else {
                tempSecurityTokenService.invalidate(player);

                tntClientApi.send(new ServerboundWebToken(player));
            }
        });
    }
}
