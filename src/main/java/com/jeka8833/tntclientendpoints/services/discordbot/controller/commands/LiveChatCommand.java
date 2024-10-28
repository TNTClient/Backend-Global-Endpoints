package com.jeka8833.tntclientendpoints.services.discordbot.controller.commands;

import com.jeka8833.tntclientendpoints.services.discordbot.ReplyWrapper;
import com.jeka8833.tntclientendpoints.services.discordbot.SendErrorMessageDiscord;
import com.jeka8833.tntclientendpoints.services.discordbot.listeners.SelectMenuListener;
import com.jeka8833.tntclientendpoints.services.discordbot.listeners.SlashCommandEvent;
import com.jeka8833.tntclientendpoints.services.discordbot.models.ConnectedChatModel;
import com.jeka8833.tntclientendpoints.services.discordbot.models.ConnectedPlayerModel;
import com.jeka8833.tntclientendpoints.services.discordbot.repositories.ConnectedChatRepository;
import com.jeka8833.tntclientendpoints.services.discordbot.repositories.ConnectedPlayerRepository;
import com.jeka8833.tntclientendpoints.services.discordbot.service.commands.GlobalLiveChatService;
import com.jeka8833.tntclientendpoints.services.discordbot.service.commands.PlayerRequesterService;
import com.jeka8833.tntclientendpoints.services.discordbot.service.commands.PrivilegeChecker;
import com.jeka8833.tntclientendpoints.services.discordbot.service.mojang.MojangProfile;
import com.jeka8833.tntclientendpoints.services.discordbot.service.mojang.api.MojangApi;
import com.jeka8833.tntclientendpoints.services.general.tntclintapi.MinecraftServer;
import com.jeka8833.tntclientendpoints.services.general.tntclintapi.TNTClientApi;
import com.jeka8833.tntclientendpoints.services.general.tntclintapi.packet.serverbound.ServerboundChat;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageCreateAction;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class LiveChatCommand implements SlashCommandEvent {
    private final MojangApi mojangAPI;
    private final TNTClientApi tntClientApi;
    private final PrivilegeChecker privilegeChecker;
    private final SelectMenuListener selectMenuListener;
    private final GlobalLiveChatService globalLiveChatService;
    private final PlayerRequesterService playerRequesterService;
    private final ConnectedChatRepository connectedChatRepository;
    private final ConnectedPlayerRepository connectedPlayerRepository;

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
                                          @NotNull ReplyWrapper replyWrapper) {
        if (privilegeChecker.hasNoAccess(event, replyWrapper, "LIVE_CHAT")) return;

        switch (event.getSubcommandName()) {
            case "connect" -> connect(event, replyWrapper);
            case "disconnect" -> disconnect(event, replyWrapper);
            case "send" -> send(event, replyWrapper);

            case null, default -> replyWrapper.replyError("Invalid command usage");
        }
    }

    private void connect(@NotNull SlashCommandInteractionEvent event,
                         @NotNull ReplyWrapper replyWrapper) {
        connectedChatRepository.save(new ConnectedChatModel(
                event.getChannelIdLong(),
                event.getUser().getIdLong())
        );

        replyWrapper.replyGood("This channel is now connected to TNTClient chat.");
    }

    private void disconnect(@NotNull SlashCommandInteractionEvent event,
                            @NotNull ReplyWrapper replyWrapper) {
        long chatID = event.getOption("channel", event.getChannelIdLong(), OptionMapping::getAsLong);

        connectedChatRepository.deleteById(chatID);

        replyWrapper.replyGood("Channel is now disconnected from TNTClient chat.");
    }

    private void send(@NotNull SlashCommandInteractionEvent event,
                      @NotNull ReplyWrapper replyWrapper) {
        String message = event.getOption("message", "", OptionMapping::getAsString);
        if (message.isBlank()) throw new SendErrorMessageDiscord("Message cannot be empty");

        UUID receiver = playerRequesterService.getProfileUuid("receiver", event)
                .orElse(null);

        MinecraftServer server = event.getOption("server", MinecraftServer.GLOBAL, optionMapping -> {
            try {
                return MinecraftServer.valueOf(optionMapping.getAsString());
            } catch (Exception e) {
                return MinecraftServer.GLOBAL;
            }
        });

        Collection<SelectOption> options = getPlayerOptions(event.getUser().getIdLong());

        WebhookMessageCreateAction<?> messageCreateAction = replyWrapper.createCustomReply()
                .sendMessage("Select the account on whose behalf you want to send the message:");

        selectMenuListener.sendMessageWithOptions(messageCreateAction, options, (selected, selectReplyWrapper) -> {
            UUID sender = UUID.fromString(selected.getFirst());
            if (sender.equals(ServerboundChat.EMPTY_USER)) {
                globalLiveChatService.sendGlobalWarning(event.getUser(), "Send message using unknown account");
            }

            tntClientApi.send(new ServerboundChat(sender, receiver, server, message));

            selectReplyWrapper.replyGood("Message sent successfully");
        });
    }

    private Collection<SelectOption> getPlayerOptions(long discordID) {
        Collection<ConnectedPlayerModel> connectedPlayerModels =
                connectedPlayerRepository.findAllByDiscord(discordID, Limit.of(SelectMenu.OPTIONS_MAX_AMOUNT - 1));

        Set<UUID> playersUUID = connectedPlayerModels.stream()
                .map(ConnectedPlayerModel::getPlayer)
                .collect(Collectors.toSet());

        Map<UUID, MojangProfile> profiles = mojangAPI.getProfiles(playersUUID);

        Collection<SelectOption> options = new ArrayList<>(profiles.size() + 1);
        options.add(SelectOption.of("Without username", ServerboundChat.EMPTY_USER.toString()));

        for (UUID playerUUID : playersUUID) {
            MojangProfile profile = profiles.get(playerUUID);

            options.add(SelectOption.of(profile.getNameAndUuidAsText(), playerUUID.toString()));
        }

        return options;
    }
}
