package com.jeka8833.tntclientendpoints.services.discordbot.listeners;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageCreateAction;
import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class SelectMenuListener extends ListenerAdapter {
    private final String prefix = String.valueOf(System.currentTimeMillis());
    private final AtomicInteger counter = new AtomicInteger();

    private final Cache<String, SelectMenuEvent> cache = new Cache2kBuilder<String, SelectMenuEvent>() {
    }
            .expireAfterWrite(15, TimeUnit.MINUTES)
            .build();

    public SelectMenuListener(JDA jda) {
        jda.addEventListener(this);
    }

    @Override
    public void onStringSelectInteraction(@NotNull StringSelectInteractionEvent event) {
        SelectMenuEvent consumer = cache.peekAndRemove(event.getComponentId());
        if (consumer == null) return;

        consumer.onSelect(event.getValues(), event.getHook());
    }

    public void sendMessageWithOptions(WebhookMessageCreateAction<?> message, Collection<SelectOption> options,
                                       SelectMenuEvent event) {
        String key = prefix + counter.getAndIncrement();

        cache.put(key, event);

        message.addActionRow(StringSelectMenu.create(key)
                .addOptions(options.stream()
                        .limit(SelectMenu.OPTIONS_MAX_AMOUNT)
                        .toList()
                )
                .build()
        ).queue();
    }
}
