package com.jeka8833.tntclientendpoints.services.discordbot.listeners;

import com.jeka8833.tntclientendpoints.services.discordbot.ReplyWrapper;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;

public interface SlashCommandEvent {
    CommandData getCommandData();

    void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event, @NotNull ReplyWrapper replyWrapper);
}
