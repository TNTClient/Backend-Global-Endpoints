package com.jeka8833.tntclientendpoints.services.discordbot.controller;

import com.jeka8833.tntclientendpoints.services.discordbot.service.discordbot.DiscordTokenManagerService;
import com.jeka8833.tntclientendpoints.services.discordbot.service.discordbot.commands.GlobalLiveChatService;
import com.jeka8833.tntclientendpoints.services.general.tntclintapi.MinecraftServer;
import com.jeka8833.tntclientendpoints.services.general.tntclintapi.TNTClientApi;
import com.jeka8833.tntclientendpoints.services.general.tntclintapi.packet.clientbound.ClientboundChat;
import com.jeka8833.tntclientendpoints.services.general.tntclintapi.packet.clientbound.ClientboundDiscordToken;
import com.jeka8833.tntclientendpoints.services.general.tntclintapi.packet.serverbound.ServerboundChat;
import com.jeka8833.tntclientendpoints.services.general.util.ChatColor;
import org.springframework.stereotype.Component;

@Component("discord-tntClientApiController")
class TNTClientApiController {

    public TNTClientApiController(TNTClientApi tntClientApi, DiscordTokenManagerService discordTokenManagerService,
                                  GlobalLiveChatService globalLiveChatService) {
        tntClientApi.registerListener(ClientboundDiscordToken.class, packet -> {
            boolean connectStatus = discordTokenManagerService.validateAndConnect(packet.getPlayer(), packet.getCode());

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
                globalLiveChatService.sendMinecraftChatMessage(
                        packet.getSender(), packet.getReceiver(), packet.getServer(), packet.getMessage()));
    }
}