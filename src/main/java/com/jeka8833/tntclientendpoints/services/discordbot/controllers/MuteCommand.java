package com.jeka8833.tntclientendpoints.services.discordbot.controllers;

import com.jeka8833.tntclientendpoints.services.discordbot.models.DiscordUser;
import com.jeka8833.tntclientendpoints.services.discordbot.services.DiscordPrivilegeChecker;
import com.jeka8833.tntclientendpoints.services.discordbot.services.slashcommand.SlashCommandListener;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MuteCommand implements SlashCommandListener {
    private final DiscordPrivilegeChecker discordPrivilegeChecker;

    @Override
    public String getName() {
        return "mute";
    }

    @Override
    public Mono<?> handle(ChatInputInteractionEvent event) {
        return Mono.fromCallable(() -> {
            if (!discordPrivilegeChecker.hasPrivilege(event, "MUTE")) return null;

            Optional<ApplicationCommandInteractionOption> addCommand = event.getOption("add");
            Optional<ApplicationCommandInteractionOption> removeCommand = event.getOption("remove");
            Optional<ApplicationCommandInteractionOption> listCommand = event.getOption("list");

            if (addCommand.isPresent()) {
                addCommand(event, userID, addCommand.get());
            } else if (removeCommand.isPresent()) {
                removeCommand(event, userID, removeCommand.get());
            } else if (listCommand.isPresent()) {
                listCommand(event);
            } else {
                return MessageUtil.printErrorMessage(event, "Invalid command usage.");
            }

            return null;
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
