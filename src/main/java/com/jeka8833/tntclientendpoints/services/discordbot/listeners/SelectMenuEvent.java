package com.jeka8833.tntclientendpoints.services.discordbot.listeners;

import com.jeka8833.tntclientendpoints.services.discordbot.DeferReplyWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@FunctionalInterface
public interface SelectMenuEvent {
    void onSelect(@NotNull List<String> selected, @NotNull DeferReplyWrapper replyWrapper);
}
