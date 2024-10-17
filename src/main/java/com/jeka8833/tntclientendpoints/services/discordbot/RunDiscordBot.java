package com.jeka8833.tntclientendpoints.services.discordbot;


import com.jeka8833.tntclientendpoints.services.mojang.MojangServiceRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RunDiscordBot {
    public static void main(String[] args) {
        SpringApplication.run(new Class[]{RunDiscordBot.class, MojangServiceRunner.class}, args);
    }
}
