package com.jeka8833.tntclientendpoints.services.discordbot.service.discordbot.commands;

import com.jeka8833.tntclientendpoints.services.discordbot.exceptions.SendErrorMessageDiscord;
import com.jeka8833.tntclientendpoints.services.discordbot.models.DiscordUserModel;
import com.jeka8833.tntclientendpoints.services.discordbot.repositories.DiscordUserRepository;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DiscordPrivilegeCheckerService {
    private final DiscordUserRepository userRepository;

    public void throwIfNoAccess(@NotNull SlashCommandInteractionEvent event, String role)
            throws SendErrorMessageDiscord {
        long userID = event.getUser().getIdLong();

        if (hasOneOfRoles(userID, role)) return;

        throw new SendErrorMessageDiscord("You don't have access to this command.");
    }

    private boolean hasOneOfRoles(long userID, String... roles) {
        if (roles.length == 0) return true;

        Optional<DiscordUserModel> userOptional = userRepository.findById(userID);
        if (userOptional.isEmpty()) return false;

        String[] userRoles = userOptional.get().getRoles().split(",");
        for (String role : userRoles) {
            for (String r : roles) {
                if (r.equalsIgnoreCase(role)) return true;
            }
        }

        return false;
    }
}
