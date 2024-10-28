package com.jeka8833.tntclientendpoints.services.discordbot.controller;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

@Component
public class JDAListeners {
    public JDAListeners(ListenerAdapter[] listenerAdapters, JDA jda) {
        jda.addEventListener((Object[]) listenerAdapters);
    }
}
