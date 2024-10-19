package com.jeka8833.tntclientendpoints.services.discordbot.service;

import com.jeka8833.tntclientendpoints.services.discordbot.models.ConnectedChat;
import com.jeka8833.tntclientendpoints.services.discordbot.repositories.ConnectedChatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LiveChatService {

    private final ConnectedChatRepository connectedChatRepository;
    private final JDA jda;

    public void sendGlobalMessage(MessageEmbed message) {
        for (ConnectedChat connectedChat : connectedChatRepository.findAll()) {
            try {
                TextChannel channel = jda.getTextChannelById(connectedChat.getChatID());
                if (channel == null) {
                    connectedChatRepository.deleteById(connectedChat.getChatID());

                    log.warn("Delete global chat for: {}", connectedChat.getChatID());
                    continue;
                }

                channel.sendMessageEmbeds(message).queue();
            } catch (Exception e) {
                log.warn("Failed to send global message to: {}", connectedChat.getChatID(), e);
            }
        }
    }
}