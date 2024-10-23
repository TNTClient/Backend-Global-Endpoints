package com.jeka8833.tntclientendpoints.services.discordbot.service.mojang;

import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record MojangProfile(@Nullable String name, @Nullable UUID uuid) {
    public boolean isCompleted() {
        return name != null && uuid != null;
    }

    public String getNameAndUuidAsText() {
        if (name == null && uuid == null) {
            return "Unknown";
        } else if (name == null) {
            return uuid.toString();
        } else if (uuid == null) {
            return name;
        } else {
            return name + " (" + uuid + ")";
        }
    }
}
