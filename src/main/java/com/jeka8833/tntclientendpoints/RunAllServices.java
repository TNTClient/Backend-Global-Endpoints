package com.jeka8833.tntclientendpoints;

import com.jeka8833.tntclientendpoints.services.discordbot.RunDiscordBot;
import com.jeka8833.tntclientendpoints.services.restapi.RunRestApi;
import org.springframework.boot.SpringApplication;

public class RunAllServices {
    public static void main(String[] args) {
        SpringApplication.run(new Class[]{RunRestApi.class, RunDiscordBot.class}, args);
    }
}
