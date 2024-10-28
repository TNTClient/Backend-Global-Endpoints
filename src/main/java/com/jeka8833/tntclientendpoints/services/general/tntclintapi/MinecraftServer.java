package com.jeka8833.tntclientendpoints.services.general.tntclintapi;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;

@Getter
@RequiredArgsConstructor
public enum MinecraftServer {
    HYPIXEL("Hypixel", "Hypixel"),
    TNT_RUN("TNTRun.net", "Odyssey"),
    GLOBAL("Global", "Unknown"),
    ;

    private final String readableName;
    private final String tntApiName;

    public static MinecraftServer fromTntApiName(@Nullable String name) {
        for (MinecraftServer server : MinecraftServer.values()) {
            if (server.getReadableName().equalsIgnoreCase(name)) {
                return server;
            }
        }

        return GLOBAL;
    }
}
