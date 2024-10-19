package com.jeka8833.tntclientendpoints.services.discordbot.listeners;

import com.jeka8833.tntclientendpoints.services.discordbot.DeferReplyWrapper;
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
public class SlashCommandListener extends ListenerAdapter {
    public final Map<String, SlashCommand> slashCommands;

    public SlashCommandListener(Collection<SlashCommand> slashCommands, JDA jda) {
        this.slashCommands = getCommandsMap(slashCommands);

        jda.addEventListener(this);
        jda.updateCommands()
                .addCommands(getCommandsData(slashCommands))
                .queue();
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        SlashCommand slashCommand = slashCommands.get(event.getName());
        if (slashCommand == null) return;

        try (var deferReplay = new DeferReplyWrapper(event)) {
            try {
                slashCommand.onSlashCommandInteraction(event, deferReplay);
            } catch (Exception e) {
                log.warn("Slash command {} has error: {}", event.getName(), e.getMessage());

                deferReplay.replyError("Internal server error");
            }
        }
    }

    private static Collection<CommandData> getCommandsData(Collection<SlashCommand> slashCommands) {
        return slashCommands.stream()
                .map(SlashCommand::getCommandData)
                .toList();
    }

    private static Map<String, SlashCommand> getCommandsMap(Collection<SlashCommand> slashCommands) {
        Map<String, SlashCommand> commandsMap = new HashMap<>(slashCommands.size());

        for (SlashCommand command : slashCommands) {
            commandsMap.put(command.getCommandData().getName(), command);
        }

        return commandsMap;
    }
}
