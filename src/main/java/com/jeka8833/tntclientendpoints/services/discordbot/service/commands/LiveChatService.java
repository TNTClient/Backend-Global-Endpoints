package com.jeka8833.tntclientendpoints.services.discordbot.service.commands;

import com.jeka8833.tntclientendpoints.services.discordbot.models.ConnectedChatModel;
import com.jeka8833.tntclientendpoints.services.discordbot.repositories.ConnectedChatRepository;
import com.jeka8833.tntclientendpoints.services.discordbot.service.mojang.MojangProfile;
import com.jeka8833.tntclientendpoints.services.discordbot.service.mojang.api.MojangAPI;
import com.jeka8833.tntclientendpoints.services.shared.tntclintapi.MinecraftServer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.time.ZonedDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LiveChatService {

    private final JDA jda;
    private final MojangAPI mojangAPI;
    private final ConnectedChatRepository connectedChatRepository;

    public void sendGlobalMinecraftMessage(@NotNull UUID player, @Nullable UUID receiver,
                                           @NotNull MinecraftServer server, @NotNull String message) {
        MojangProfile profile = mojangAPI.getProfile(player);

        EmbedBuilder builder = new EmbedBuilder()
                .setColor(Color.CYAN)
                .setAuthor(profile.getNameAndUuidAsText(), null, profile.getAvatarUrl())
                .setDescription(message)
                .setTimestamp(ZonedDateTime.now());

        if (receiver != null) {
            MojangProfile receiverProfile = mojangAPI.getProfile(receiver);

            builder.setFooter("From " + receiverProfile.getNameAndUuidAsText() + "; Server: " + server.getName(),
                    receiverProfile.getAvatarUrl());
        } else {
            builder.setFooter("Server: " + server.getName());
        }

        sendGlobalMessage(builder.build());
    }

    public void sendGlobalWarning(User user, String message) {
        sendGlobalMessage(new EmbedBuilder()
                .setColor(Color.YELLOW)
                .setAuthor(user.getName() + " (" + user.getId() + ")", null, user.getAvatarUrl())
                .setDescription(message)
                .build()
        );
    }

    public void sendGlobalMessage(MessageEmbed message) {
        for (ConnectedChatModel connectedChatModel : connectedChatRepository.findAll()) {
            try {
                TextChannel channel = jda.getTextChannelById(connectedChatModel.getChatID());
                if (channel == null) {
                    connectedChatRepository.deleteById(connectedChatModel.getChatID());

                    log.warn("Delete global chat for: {}", connectedChatModel.getChatID());
                    continue;
                }

                channel.sendMessageEmbeds(message).queue();
            } catch (Exception e) {
                log.warn("Failed to send global message to: {}", connectedChatModel.getChatID(), e);
            }
        }
    }
}