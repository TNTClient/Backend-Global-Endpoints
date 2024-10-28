package com.jeka8833.tntclientendpoints.services.discordbot.listeners;

import com.jeka8833.tntclientendpoints.services.discordbot.ReplyWrapper;
import com.jeka8833.tntclientendpoints.services.discordbot.SendErrorMessageDiscord;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Component
public class SelectMenuListener extends ListenerAdapter {
    private final String prefix = String.valueOf(System.currentTimeMillis());
    private final AtomicInteger counter = new AtomicInteger();

    private final Cache<String, SelectMenuEvent> cache = new Cache2kBuilder<String, SelectMenuEvent>() {
    }
            .expireAfterWrite(15, TimeUnit.MINUTES)
            .build();

    @Override
    public void onStringSelectInteraction(@NotNull StringSelectInteractionEvent event) {
        event.deferEdit().queue(hook -> hook.deleteOriginal().queue());

        SelectMenuEvent consumer = cache.peekAndRemove(event.getComponentId());
        if (consumer == null) return;

        try (var deferReply = new ReplyWrapper(event)) {
            try {
                consumer.onSelect(event.getValues(), deferReply);
            } catch (SendErrorMessageDiscord e) {
                deferReply.replyError(e.getMessage());
            } catch (Exception e) {
                log.warn("Select menu has error, selected: {}", event.getValues(), e);

                deferReply.replyError("Internal server error");
            }
        }
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
