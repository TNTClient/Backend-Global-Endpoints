package com.jeka8833.tntclientendpoints.services.discordbot.configs;

import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.presence.ClientActivity;
import discord4j.core.object.presence.ClientPresence;
import discord4j.rest.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Discord4jConfig {
    private final String botToken;

    Discord4jConfig(@Value("${bot.token}") String botToken) {
        this.botToken = botToken;
    }

    @Bean
    public GatewayDiscordClient gatewayDiscordClient() {
        return DiscordClientBuilder.create(botToken).build()
                .gateway()
                .setInitialPresence(ignore -> ClientPresence.online(ClientActivity.listening("to /commands")))
                .login()
                .block();
    }

    @Bean
    public RestClient discordRestClient(GatewayDiscordClient client) {
        return client.getRestClient();
    }
}
