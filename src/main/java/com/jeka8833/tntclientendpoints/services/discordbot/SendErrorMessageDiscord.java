package com.jeka8833.tntclientendpoints.services.discordbot;

public class SendErrorMessageDiscord extends RuntimeException {
    public SendErrorMessageDiscord(String message) {
        super(message);
    }
}
