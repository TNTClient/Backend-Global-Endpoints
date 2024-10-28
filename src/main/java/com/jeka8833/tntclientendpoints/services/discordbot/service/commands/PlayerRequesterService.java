package com.jeka8833.tntclientendpoints.services.discordbot.service.commands;


import com.jeka8833.tntclientendpoints.services.discordbot.DeferReplyWrapper;
import com.jeka8833.tntclientendpoints.services.discordbot.service.mojang.api.MojangApi;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlayerRequesterService {
    private final MojangApi mojangAPI;

    public Optional<UUID> getProfileUuidOrReplay(String optionName, @NotNull SlashCommandInteractionEvent event,
                                                 @NotNull DeferReplyWrapper deferReply) {
        Optional<UUID> playerUUID = getProfileUuid(optionName, event);

        if (playerUUID.isEmpty()) {
            deferReply.replyError("Invalid player name or uuid. Or maybe Mojang API is down.");
        }

        return playerUUID;
    }

    public Optional<UUID> getProfileUuid(String optionName, @NotNull SlashCommandInteractionEvent event) {
        return event.getOption(optionName, Optional.empty(), optionMapping ->
                getProfileUUID(optionMapping.getAsString()));
    }

    private Optional<UUID> getProfileUUID(String uuidOrName) {
        if (MojangApi.isPlayerName(uuidOrName)) {
            return Optional.ofNullable(mojangAPI.getProfile(uuidOrName).uuid());
        } else {
            try {
                return Optional.of(UUID.fromString(uuidOrName));
            } catch (Exception e) {
                return Optional.empty();
            }
        }
    }
}
