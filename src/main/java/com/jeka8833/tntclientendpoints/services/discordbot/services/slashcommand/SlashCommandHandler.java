package com.jeka8833.tntclientendpoints.services.discordbot.services.slashcommand;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.Collection;
import java.util.List;

@Component
public class SlashCommandHandler {
    private final Collection<SlashCommandListener> commands;

    public SlashCommandHandler(List<SlashCommandListener> slashCommands, GatewayDiscordClient client) {
        commands = slashCommands;

        client.on(ChatInputInteractionEvent.class, this::handle).subscribe();
    }

    public Publisher<?> handle(ChatInputInteractionEvent event) {
        return Flux.fromIterable(commands)
                .filter(command ->
                        command.getName().equalsIgnoreCase(event.getCommandName()))
                .next()
                .flatMap(command ->
                        event.deferReply().then(command.handle(event)));
    }
}
