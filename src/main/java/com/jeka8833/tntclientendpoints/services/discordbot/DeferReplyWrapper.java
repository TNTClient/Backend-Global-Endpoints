package com.jeka8833.tntclientendpoints.services.discordbot;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessagePollData;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class DeferReplyWrapper implements AutoCloseable {
    private final SlashCommandInteractionEvent event;
    private final CompletableFuture<?> completableFuture;

    public DeferReplyWrapper(SlashCommandInteractionEvent event) {
        completableFuture = event.deferReply(true).submit();

        this.event = event;
    }

    public void reply(@NotNull MessageCreateData message) {
        completableFuture.cancel(true);

        event.getHook().sendMessage(message).setEphemeral(true).queue();
    }

    public void replyPoll(@NotNull MessagePollData poll) {
        completableFuture.cancel(true);

        event.getHook().sendMessagePoll(poll).setEphemeral(true).queue();
    }

    public void reply(@NotNull String content) {
        completableFuture.cancel(true);

        event.getHook().sendMessage(content).setEphemeral(true).queue();
    }

    public void replyEmbeds(@NotNull Collection<? extends MessageEmbed> embeds) {
        completableFuture.cancel(true);

        event.getHook().sendMessageEmbeds(embeds).setEphemeral(true).queue();
    }

    public void replyEmbeds(@NotNull MessageEmbed embed, @NotNull MessageEmbed... embeds) {
        completableFuture.cancel(true);

        event.getHook().sendMessageEmbeds(embed, embeds).setEphemeral(true).queue();
    }

    public void replyComponents(@NotNull Collection<? extends LayoutComponent> components) {
        completableFuture.cancel(true);

        event.getHook().sendMessageComponents(components).setEphemeral(true).queue();
    }

    public void replyComponents(@NotNull LayoutComponent component, @NotNull LayoutComponent... other) {
        completableFuture.cancel(true);

        event.getHook().sendMessageComponents(component, other).setEphemeral(true).queue();
    }

    public void replyFormat(@NotNull String format, @NotNull Object... args) {
        completableFuture.cancel(true);

        event.getHook().sendMessageFormat(format, args).setEphemeral(true).queue();
    }

    public void replyFiles(@NotNull Collection<? extends FileUpload> files) {
        completableFuture.cancel(true);

        event.getHook().sendFiles(files).setEphemeral(true).queue();
    }

    public void replyFiles(@NotNull FileUpload... files) {
        completableFuture.cancel(true);

        event.getHook().sendFiles(files).setEphemeral(true).queue();
    }

    public void replyError(@NotNull String errorMessage) {
        replyEmbeds(new EmbedBuilder()
                .setTitle("Error")
                .setDescription(errorMessage)
                .setColor(Color.RED)
                .build());
    }

    public void replyGood(@NotNull String goodMessage) {
        replyEmbeds(new EmbedBuilder()
                .setTitle("Success")
                .setDescription(goodMessage)
                .setColor(Color.GREEN)
                .build());
    }

    @Override
    public void close() {
        if (completableFuture.complete(null)) {
            replyError("Something went wrong... Bot has no message to reply");
        }
    }
}
