package com.jeka8833.tntclientendpoints.services.discordbot.listeners;

import net.dv8tion.jda.api.interactions.InteractionHook;

import java.util.List;

@FunctionalInterface
public interface SelectMenuEvent {
    void onSelect(List<String> selected, InteractionHook hook);
}
