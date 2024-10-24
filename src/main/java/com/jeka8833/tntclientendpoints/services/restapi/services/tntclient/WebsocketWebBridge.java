package com.jeka8833.tntclientendpoints.services.restapi.services.tntclient;

import com.jeka8833.tntclientendpoints.services.restapi.services.web.security.TempSecurityTokenService;
import com.jeka8833.tntclientendpoints.services.shared.tntclintapi.TNTClientApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
public class WebsocketWebBridge {
    private final TNTClientApi tntClientApi;
    private final TempSecurityTokenService tempSecurityTokenService;

    public WebsocketWebBridge(TNTClientApi tntClientApi, TempSecurityTokenService tempSecurityTokenService) {
        this.tntClientApi = tntClientApi;
        this.tempSecurityTokenService = tempSecurityTokenService;
    }
}
