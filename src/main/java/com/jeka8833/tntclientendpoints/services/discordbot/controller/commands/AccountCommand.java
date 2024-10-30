package com.jeka8833.tntclientendpoints.services.discordbot.controller.commands;

import com.jeka8833.tntclientendpoints.services.discordbot.listeners.SlashCommandEvent;
import com.jeka8833.tntclientendpoints.services.discordbot.service.discordbot.DiscordTokenManagerService;
import com.jeka8833.tntclientendpoints.services.discordbot.service.discordbot.ReplyWrapper;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class AccountCommand implements SlashCommandEvent {
    private final DiscordTokenManagerService discordTokenManagerService;

    @Override
    public CommandData getCommandData() {
        return Commands.slash("account", "Manage your connected Minecraft accounts.").addSubcommands(

                new SubcommandData("add", "Links your Discord account to your Minecraft account."),

                new SubcommandData("remove",
                        "Unlinks your Discord account from all your Minecraft accounts.")
        );
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event,
                                          @NotNull ReplyWrapper replyWrapper) {
        switch (event.getSubcommandName()) {
            case "add" -> add(event, replyWrapper);
            case "remove" -> remove(event, replyWrapper);

            case null, default -> replyWrapper.replyError("Invalid command usage");
        }
    }

    private void add(@NotNull SlashCommandInteractionEvent event,
                     @NotNull ReplyWrapper replyWrapper) {
        long discordID = event.getUser().getIdLong();
        int token = discordTokenManagerService.generateToken(discordID);

        replyWrapper.replyGood("Write the command \"@discordlink " + token +
                "\" into the game chat using TNTClient. You have 1 minute to do this," +
                " if you have not done it in time, generate the token again.");
    }

    private void remove(@NotNull SlashCommandInteractionEvent event,
                        @NotNull ReplyWrapper replyWrapper) {
        long discordID = event.getUser().getIdLong();

        discordTokenManagerService.disconnect(discordID);

        replyWrapper.replyGood(
                "All your Minecraft accounts have been unlinked from your Discord account.");
    }
}
