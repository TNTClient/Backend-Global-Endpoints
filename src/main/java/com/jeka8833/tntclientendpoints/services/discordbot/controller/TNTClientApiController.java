package com.jeka8833.tntclientendpoints.services.discordbot.controller;

import com.jeka8833.tntclientendpoints.services.discordbot.service.ConnectTokenService;
import com.jeka8833.tntclientendpoints.services.shared.minecraft.ChatColor;
import com.jeka8833.tntclientendpoints.services.shared.tntclintapi.TNTClientApi;
import com.jeka8833.tntclientendpoints.services.shared.tntclintapi.packet.clientbound.ClientboundDiscordTocken;
import com.jeka8833.tntclientendpoints.services.shared.tntclintapi.packet.serverbound.ServerboundChat;
import org.springframework.stereotype.Component;

@Component
public class TNTClientApiController {

    public TNTClientApiController(TNTClientApi tntClientApi, ConnectTokenService connectTokenService) {
        tntClientApi.registerListener(ClientboundDiscordTocken.class, packet -> {
            boolean connectStatus = connectTokenService.connect(packet.getPlayer(), packet.getCode());

            if (connectStatus) {
                tntClientApi.send(new ServerboundChat(packet.getPlayer(),
                        ChatColor.GREEN + "Your discord account was linked to your TNTClient. If " +
                                "it was not you, write the command \"@discordlink remove\"."));
            } else {
                tntClientApi.send(new ServerboundChat(packet.getPlayer(),
                        ChatColor.RED + "Incorrect code or you did not have time to enter it."));
            }
        });
    }
}