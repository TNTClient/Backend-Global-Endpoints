package com.jeka8833.tntclientendpoints.services.discordbot.commands;

import com.jeka8833.tntclientendpoints.services.discordbot.DeferReplyWrapper;
import com.jeka8833.tntclientendpoints.services.discordbot.listeners.SlashCommand;
import com.jeka8833.tntclientendpoints.services.discordbot.service.ConnectTokenService;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountCommand implements SlashCommand {
    private final ConnectTokenService connectTokenService;

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
                                          @NotNull DeferReplyWrapper deferReplyWrapper) {
        switch (event.getSubcommandName()) {
            case "add" -> add(event, deferReplyWrapper);
            case "remove" -> remove(event, deferReplyWrapper);

            case null, default -> deferReplyWrapper.replyError("Invalid command usage");
        }
    }

    private void add(@NotNull SlashCommandInteractionEvent event,
                     @NotNull DeferReplyWrapper deferReplyWrapper) {
        long discordID = event.getUser().getIdLong();
        int token = connectTokenService.generateToken(discordID);

        deferReplyWrapper.replyGood("Write the command \"@discordlink " + token +
                "\" into the game chat using TNTClient. You have 1 minute to do this," +
                " if you have not done it in time, generate the token again.");
    }

    private void remove(@NotNull SlashCommandInteractionEvent event,
                        @NotNull DeferReplyWrapper deferReplyWrapper) {
        long discordID = event.getUser().getIdLong();

        connectTokenService.disconnect(discordID);

        deferReplyWrapper.replyGood(
                "All your Minecraft accounts have been unlinked from your Discord account.");
    }
}
