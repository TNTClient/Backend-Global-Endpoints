package com.jeka8833.tntclientendpoints.services.discordbot.controller;

import com.jeka8833.tntclientendpoints.services.discordbot.service.ConnectTokenService;
import com.jeka8833.tntclientendpoints.services.discordbot.service.commands.LiveChatService;
import com.jeka8833.tntclientendpoints.services.general.minecraft.ChatColor;
import com.jeka8833.tntclientendpoints.services.general.tntclintapi.MinecraftServer;
import com.jeka8833.tntclientendpoints.services.general.tntclintapi.TNTClientApi;
import com.jeka8833.tntclientendpoints.services.general.tntclintapi.packet.clientbound.ClientboundChat;
import com.jeka8833.tntclientendpoints.services.general.tntclintapi.packet.clientbound.ClientboundDiscordTocken;
import com.jeka8833.tntclientendpoints.services.general.tntclintapi.packet.serverbound.ServerboundChat;
import org.springframework.stereotype.Component;

@Component("discord-tntClientApiController")
public class TNTClientApiController {

    public TNTClientApiController(TNTClientApi tntClientApi, ConnectTokenService connectTokenService,
                                  LiveChatService liveChatService) {
        tntClientApi.registerListener(ClientboundDiscordTocken.class, packet -> {
            boolean connectStatus = connectTokenService.connect(packet.getPlayer(), packet.getCode());

            if (connectStatus) {
                tntClientApi.send(new ServerboundChat(null, packet.getPlayer(), MinecraftServer.GLOBAL,
                        ChatColor.GREEN + "Your discord account was linked to your TNTClient. If " +
                                "it was not you, write the command \"@discordlink remove\"."));
            } else {
                tntClientApi.send(new ServerboundChat(null, packet.getPlayer(), MinecraftServer.GLOBAL,
                        ChatColor.RED + "Incorrect code or you did not have time to enter it."));
            }
        });

        tntClientApi.registerListener(ClientboundChat.class, packet ->
                liveChatService.sendGlobalMinecraftMessage(
                        packet.getSender(), packet.getReceiver(), packet.getServer(), packet.getMessage()));
    }
}