package com.jeka8833.tntclientendpoints.services.discordbot.service.mojang.dtos;

import org.jetbrains.annotations.Nullable;

public record MojangProfileDto(@Nullable String name, @Nullable String id) {
    public boolean isFullAbsent() {
        return name == null && id == null;
    }
}
