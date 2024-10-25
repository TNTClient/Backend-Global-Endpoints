package com.jeka8833.tntclientendpoints.services.discordbot.controller.commands;

import com.jeka8833.tntclientendpoints.services.discordbot.DeferReplyWrapper;
import com.jeka8833.tntclientendpoints.services.discordbot.listeners.SelectMenuListener;
import com.jeka8833.tntclientendpoints.services.discordbot.listeners.SlashCommandEvent;
import com.jeka8833.tntclientendpoints.services.discordbot.models.ConnectedChatModel;
import com.jeka8833.tntclientendpoints.services.discordbot.repositories.ConnectedChatRepository;
import com.jeka8833.tntclientendpoints.services.discordbot.service.commands.PlayerRequesterService;
import com.jeka8833.tntclientendpoints.services.discordbot.service.commands.PrivilegeChecker;
import com.jeka8833.tntclientendpoints.services.shared.tntclintapi.MinecraftServer;
import com.jeka8833.tntclientendpoints.services.shared.tntclintapi.TNTClientApi;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LiveChatCommand implements SlashCommandEvent {

    private final TNTClientApi tntClientApi;
    private final PrivilegeChecker privilegeChecker;
    private final PlayerRequesterService playerRequesterService;
    private final ConnectedChatRepository connectedChatRepository;
    private final SelectMenuListener selectMenuListener;

    @Override
    public CommandData getCommandData() {
        return Commands.slash("livechat", "Controls the Mute list.").addSubcommands(

                new SubcommandData("connect", "Connect this channel to TNTClient chat."),

                new SubcommandData("disconnect",
                        "Disconnect this or selected channel from TNTClient chat.")

                        .addOptions(new OptionData(OptionType.INTEGER, "channel",
                                "The channel to disconnect from TNTClient chat.", false)
                        ),

                new SubcommandData("send",
                        "Allows you to send a message to all players or to one selected player.")

                        .addOptions(
                                new OptionData(OptionType.STRING, "message",
                                        "The message you want to send.", true)
                                        .setMinLength(1),

                                new OptionData(OptionType.STRING, "receiver",
                                        "The recipient of the message, if the field is not specified, " +
                                                "the message is public.", false)
                                        .setRequiredLength(1, 36),

                                new OptionData(OptionType.STRING, "server",
                                        "Specifies the server on which the message will be visible.",
                                        false)

                                        .addChoice(MinecraftServer.GLOBAL.getReadableName(),
                                                MinecraftServer.GLOBAL.name())

                                        .addChoice(MinecraftServer.HYPIXEL.getReadableName(),
                                                MinecraftServer.HYPIXEL.name())

                                        .addChoice(MinecraftServer.TNT_RUN.getReadableName(),
                                                MinecraftServer.TNT_RUN.name())
                        )
        );
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event,
                                          @NotNull DeferReplyWrapper deferReplyWrapper) {
        if (privilegeChecker.hasNoAccess(event, deferReplyWrapper, "LIVE_CHAT")) return;

        switch (event.getSubcommandName()) {
            case "connect" -> connect(event, deferReplyWrapper);
            case "disconnect" -> disconnect(event, deferReplyWrapper);
            case "send" -> send(event, deferReplyWrapper);

            case null, default -> deferReplyWrapper.replyError("Invalid command usage");
        }
    }

    private void connect(@NotNull SlashCommandInteractionEvent event,
                         @NotNull DeferReplyWrapper deferReplyWrapper) {
        connectedChatRepository.save(new ConnectedChatModel(
                event.getChannelIdLong(),
                event.getUser().getIdLong())
        );

        deferReplyWrapper.replyGood("This channel is now connected to TNTClient chat.");
    }

    private void disconnect(@NotNull SlashCommandInteractionEvent event,
                            @NotNull DeferReplyWrapper deferReplyWrapper) {
        long chatID = event.getOption("channel", event.getChannelIdLong(), OptionMapping::getAsLong);

        connectedChatRepository.deleteById(chatID);

        deferReplyWrapper.replyGood("Channel is now disconnected from TNTClient chat.");
    }

    private void send(@NotNull SlashCommandInteractionEvent event,
                      @NotNull DeferReplyWrapper deferReplyWrapper) {
        String message = event.getOption("message", "", OptionMapping::getAsString);

        Optional<UUID> playerOpt = playerRequesterService.getProfileUuid("receiver", event);

        MinecraftServer server = event.getOption("server", MinecraftServer.GLOBAL, optionMapping -> {
            try {
                return MinecraftServer.valueOf(optionMapping.getAsString());
            } catch (Exception e) {
                return MinecraftServer.GLOBAL;
            }
        });

        selectMenuListener.sendMessageWithOptions(deferReplyWrapper.createCustomReply().sendMessage("Choose a player"), List.of(
                SelectOption.of("None", "none"),
                SelectOption.of("All", "all")), (selected, hook) -> {
            System.out.println(selected.getFirst());
        });
    }
}
