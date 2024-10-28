package com.jeka8833.tntclientendpoints.services.discordbot.service.commands;

import com.jeka8833.tntclientendpoints.services.discordbot.DeferReplyWrapper;
import com.jeka8833.tntclientendpoints.services.discordbot.ReplyWrapper;
import com.jeka8833.tntclientendpoints.services.discordbot.models.DiscordUserModel;
import com.jeka8833.tntclientendpoints.services.discordbot.repositories.DiscordUserRepository;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PrivilegeChecker {
    private final DiscordUserRepository userRepository;

    public boolean hasNoAccess(@NotNull SlashCommandInteractionEvent event, @NotNull ReplyWrapper deferReplay,
                               String role) {
        long userID = event.getUser().getIdLong();

        Optional<Set<String>> roles = getRoles(userID);
        if (roles.isPresent()) {
            if (roles.get().contains(role)) {
                return false;
            }
        }

        deferReplay.replyError("You don't have access to that command. Your Discord ID: " + userID);
        return true;
    }

    public Optional<Set<String>> getRoles(long userID) {
        Optional<DiscordUserModel> userOptional = userRepository.findById(userID);
        if (userOptional.isEmpty()) return Optional.empty();

        Set<String> roles = new HashSet<>(
                Arrays.asList(
                        userOptional.get().getRoles()
                                .split(",")));

        return Optional.of(roles);
    }
}
