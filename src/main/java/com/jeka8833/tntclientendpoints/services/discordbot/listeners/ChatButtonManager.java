package com.jeka8833.tntclientendpoints.services.discordbot.listeners;

import com.jeka8833.tntclientendpoints.services.discordbot.models.ActionTimedButton;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.object.component.Button;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
@EnableScheduling
public class ChatButtonManager {
    private final Map<String, ActionTimedButton> buttons = new ConcurrentHashMap<>();

    public ChatButtonManager(GatewayDiscordClient client) {
        client.on(ButtonInteractionEvent.class, this::handle).subscribe();
    }

    @Scheduled(fixedDelay = 10, timeUnit = TimeUnit.MINUTES)
    public void clearExpiredButtons() {
        buttons.values().removeIf(ActionTimedButton::isExpired);
    }

    public List<Button> registerButtonGroup(Map<Button, ActionTimedButton> buttonMap) {
        Collection<String> idsInGroup = new ArrayList<>(buttonMap.size());

        Runnable clearGroupRunnable = () -> {
            for (String id : idsInGroup) {
                buttons.remove(id);
            }
        };

        for (Map.Entry<Button, ActionTimedButton> entry : buttonMap.entrySet()) {
            String id = entry.getKey()
                    .getCustomId()
                    .orElseThrow();

            idsInGroup.add(id);

            entry.getValue().setClearGroupRunnable(clearGroupRunnable);

            buttons.put(id, entry.getValue());
        }

        return new ArrayList<>(buttonMap.keySet());
    }

    public List<Button> registerButtonGroupReusable(Map<Button, ActionTimedButton> buttonMap) {
        for (Map.Entry<Button, ActionTimedButton> entry : buttonMap.entrySet()) {
            String id = entry.getKey()
                    .getCustomId()
                    .orElseThrow();

            buttons.put(id, entry.getValue());
        }

        return new ArrayList<>(buttonMap.keySet());
    }

    public Mono<Void> handle(ButtonInteractionEvent event) {
        return Mono.justOrEmpty(buttons.get(event.getCustomId()))
                .doOnNext(ActionTimedButton::run)
                .then();
    }
}
