package com.jeka8833.tntclientendpoints.services.discordbot.service.discordbot.commands;


import com.jeka8833.tntclientendpoints.services.discordbot.exceptions.SendErrorMessageDiscord;
import com.jeka8833.tntclientendpoints.services.discordbot.service.mojang.MojangProfile;
import com.jeka8833.tntclientendpoints.services.discordbot.service.mojang.api.MojangApi;
import com.jeka8833.tntclientendpoints.services.general.util.UuidUtil;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PlayerRequesterService {
    private final MojangApi mojangAPI;

    public MojangProfile getMojangProfileOrThrow(SlashCommandInteractionEvent event, String optionName)
            throws SendErrorMessageDiscord {

        Optional<MojangProfile> mojangProfile = getMojangProfileOptional(event, optionName);

        return mojangProfile.orElseThrow(() -> new SendErrorMessageDiscord("Field " + optionName + " not found."));
    }

    public Optional<MojangProfile> getMojangProfileOptional(SlashCommandInteractionEvent event, String optionName)
            throws SendErrorMessageDiscord {

        return event.getOption(optionName, Optional.empty(), optionMapping ->
                Optional.of(getProfile(optionMapping.getAsString())));
    }

    private MojangProfile getProfile(String uuidOrName) throws SendErrorMessageDiscord {
        MojangProfile profile = MojangApi.isPlayerName(uuidOrName) ?
                mojangAPI.getProfile(uuidOrName) :
                mojangAPI.getProfile(UuidUtil.parseOrNull(uuidOrName));

        if (MojangApi.isPlayerUUID(profile.uuid())) {
            throw new SendErrorMessageDiscord("Player " + uuidOrName + " not found.");
        }

        return profile;
    }
}
