package com.jeka8833.tntclientendpoints.services.discordbot.listeners;

import com.jeka8833.tntclientendpoints.services.discordbot.exceptions.SendErrorMessageDiscord;
import com.jeka8833.tntclientendpoints.services.discordbot.service.discordbot.DeferReplyWrapper;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
class SlashCommandListener extends ListenerAdapter {
    private final Map<String, SlashCommandEvent> slashCommands;

    public SlashCommandListener(Collection<SlashCommandEvent> slashCommands, JDA jda) {
        this.slashCommands = getCommandsMap(slashCommands);

        jda.updateCommands()
                .addCommands(getCommandsData(slashCommands))
                .queue();
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        SlashCommandEvent slashCommand = slashCommands.get(event.getName());
        if (slashCommand == null) return;

        try (var deferReply = new DeferReplyWrapper(event)) {
            try {
                slashCommand.onSlashCommandInteraction(event, deferReply);
            } catch (SendErrorMessageDiscord e) {
                deferReply.replyError(e.getMessage());
            } catch (Exception e) {
                log.warn("Slash command {} has error: {}", event.getName(), e.getMessage());

                deferReply.replyError("Internal server error");
            }
        }
    }

    private static Collection<CommandData> getCommandsData(Collection<SlashCommandEvent> slashCommands) {
        return slashCommands.stream()
                .map(SlashCommandEvent::getCommandData)
                .toList();
    }

    private static Map<String, SlashCommandEvent> getCommandsMap(Collection<SlashCommandEvent> slashCommands) {
        Map<String, SlashCommandEvent> commandsMap = new HashMap<>(slashCommands.size());

        for (SlashCommandEvent command : slashCommands) {
            commandsMap.put(command.getCommandData().getName(), command);
        }

        return commandsMap;
    }
}
