package com.jeka8833.tntclientendpoints.services.discordbot.configs;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class JDAInitializer {
    private final String botToken;

    public JDAInitializer(@Value("${discord.bot.token}") String botToken) {
        this.botToken = botToken;
    }

    @Bean
    public JDA getJDA() {
        return JDABuilder.createDefault(botToken)
                .setActivity(Activity.playing("TNTClient"))
                .build();
    }
}
