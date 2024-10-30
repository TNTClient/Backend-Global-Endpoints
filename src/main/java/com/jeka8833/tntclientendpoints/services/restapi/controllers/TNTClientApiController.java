package com.jeka8833.tntclientendpoints.services.restapi.controllers;

import com.jeka8833.tntclientendpoints.services.general.tntclintapi.TNTClientApi;
import com.jeka8833.tntclientendpoints.services.general.tntclintapi.packet.clientbound.ClientboundWebToken;
import com.jeka8833.tntclientendpoints.services.restapi.services.tntclient.SecurityTokenManager;
import org.springframework.stereotype.Component;

@Component("web-tntClientApiController")
public class TNTClientApiController {

    public TNTClientApiController(TNTClientApi tntClientApi, SecurityTokenManager securityTokenManager) {
        tntClientApi.registerListener(ClientboundWebToken.class, packet ->
                securityTokenManager.generateAndSendToken(packet.getPlayer(), packet.isRegister()));
    }
}
