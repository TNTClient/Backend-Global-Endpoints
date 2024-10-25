package com.jeka8833.tntclientendpoints.services.discordbot;

import lombok.Setter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class DeferReplyWrapper implements AutoCloseable {
    private final InteractionHook hook;

    @Setter
    private boolean isReplied = false;

    public DeferReplyWrapper(IReplyCallback event) {
        event.deferReply(true).queue();

        this.hook = event.getHook().setEphemeral(true);
    }

    public void reply(@NotNull String content) {
        createCustomReply().sendMessage(content).queue();
    }

    public void replyEmbeds(@NotNull MessageEmbed embed, @NotNull MessageEmbed... embeds) {
        createCustomReply().sendMessageEmbeds(embed, embeds).queue();
    }

    public InteractionHook createCustomReply() {
        isReplied = true;

        return hook;
    }

    public void replyError(@NotNull String errorMessage) {
        replyEmbeds(new EmbedBuilder()
                .setColor(Color.RED)
                .setTitle("Error")
                .setDescription(errorMessage)
                .build());
    }

    public void replyGood(@NotNull String goodMessage) {
        replyEmbeds(new EmbedBuilder()
                .setColor(Color.GREEN)
                .setTitle("Success")
                .setDescription(goodMessage)
                .build());
    }

    @Override
    public void close() {
        if (!isReplied) {
            replyError("Something went wrong... Bot has no message to reply");
        }
    }
}
