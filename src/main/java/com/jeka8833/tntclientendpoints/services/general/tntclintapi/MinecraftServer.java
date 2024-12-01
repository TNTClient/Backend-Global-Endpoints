package com.jeka8833.tntclientendpoints.services.general.tntclintapi;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
@RequiredArgsConstructor
public enum MinecraftServer {
    HYPIXEL("Hypixel", "Hypixel"),
    TNT_RUN("TNTRun.net", "Odyssey"),
    GLOBAL("Global", "Unknown"),
    ;

    public static final MinecraftServer[] VALUES = values();

    private final String readableName;
    private final String tntApiName;

    @NotNull
    public static MinecraftServer fromTntApiName(@Nullable String name) {
        for (MinecraftServer server : VALUES) {
            if (server.getTntApiName().equalsIgnoreCase(name)) {
                return server;
            }
        }

        return GLOBAL;
    }
}
