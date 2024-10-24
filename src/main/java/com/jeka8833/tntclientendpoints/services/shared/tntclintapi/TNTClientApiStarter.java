package com.jeka8833.tntclientendpoints.services.shared.tntclintapi;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class TNTClientApiStarter {
    private final TNTClientApi tntClientAPI;

    @EventListener(ApplicationReadyEvent.class)
    public void onReady(@NotNull ApplicationReadyEvent ignoredEvent) {
        tntClientAPI.connect();
    }

    @EventListener(ContextClosedEvent.class)
    public void onClose(@NotNull ContextClosedEvent ignoredEvent) {
        tntClientAPI.disconnect();
    }
}
