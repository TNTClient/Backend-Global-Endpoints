package com.jeka8833.tntclientendpoints.services.discordbot.service.commands;

import com.jeka8833.tntclientendpoints.services.discordbot.models.ConnectedChatModel;
import com.jeka8833.tntclientendpoints.services.discordbot.repositories.ConnectedChatRepository;
import com.jeka8833.tntclientendpoints.services.discordbot.service.mojang.MojangProfile;
import com.jeka8833.tntclientendpoints.services.discordbot.service.mojang.api.MojangApi;
import com.jeka8833.tntclientendpoints.services.general.tntclintapi.MinecraftServer;
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
public class GlobalLiveChatService {
    private final JDA jda;
    private final MojangApi mojangAPI;
    private final ConnectedChatRepository connectedChatRepository;

    public void sendMinecraftChatMessage(@NotNull UUID sender, @Nullable UUID receiver,
                                         @NotNull MinecraftServer server, @NotNull String message) {
        MojangProfile senderProfile = mojangAPI.getProfile(sender);

        EmbedBuilder builder = new EmbedBuilder()
                .setColor(Color.CYAN)
                .setAuthor(senderProfile.getNameAndUuidAsText(), null, senderProfile.getAvatarUrl())
                .setDescription(message)
                .setTimestamp(ZonedDateTime.now());

        MojangProfile receiverProfile = mojangAPI.getProfile(receiver);

        if (receiverProfile.isFullAbsent()) {
            builder.setFooter("From " + receiverProfile.getNameAndUuidAsText() +
                    "; Server: " + server.getReadableName(), receiverProfile.getAvatarUrl());
        } else {
            builder.setFooter("Server: " + server.getReadableName());
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
            long chatID = connectedChatModel.getChatID();

            try {
                TextChannel channel = jda.getTextChannelById(chatID);
                if (channel == null) {
                    connectedChatRepository.deleteById(chatID);

                    log.warn("Delete global chat for: {}", chatID);
                    continue;
                }

                channel.sendMessageEmbeds(message).queue();
            } catch (Exception e) {
                log.warn("Failed to send global message to: {}", chatID, e);
            }
        }
    }
}