package com.jeka8833.tntclientendpoints.services.discordbot.services;

import com.jeka8833.tntclientendpoints.services.discordbot.models.DiscordUser;
import com.jeka8833.tntclientendpoints.services.discordbot.repositories.DiscordUserRepository;
import discord4j.core.event.domain.interaction.InteractionCreateEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DiscordPrivilegeChecker {
    private final DiscordUserRepository userRepository;

    public boolean hasPrivilege(InteractionCreateEvent event, String privilege) {
        long userID = event.getInteraction()
                .getUser()
                .getId()
                .asLong();

        Optional<DiscordUser> userOpt = userRepository.findById(userID);
        return userOpt.filter(discordUser ->
                        hasPrivilege(discordUser.getRoles(), privilege))
                .isPresent();

    }

    private static boolean hasPrivilege(String comaSeparatedPrivileges, String privileges) {
        for (String privilege : comaSeparatedPrivileges.split(",")) {
            if (privilege.equalsIgnoreCase(privileges)) return true;
        }

        return false;
    }
}
