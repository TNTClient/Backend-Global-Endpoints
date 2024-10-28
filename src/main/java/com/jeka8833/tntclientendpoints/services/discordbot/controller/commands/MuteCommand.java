package com.jeka8833.tntclientendpoints.services.discordbot.controller.commands;

import com.jeka8833.tntclientendpoints.services.discordbot.DeferReplyWrapper;
import com.jeka8833.tntclientendpoints.services.discordbot.listeners.SlashCommandEvent;
import com.jeka8833.tntclientendpoints.services.discordbot.models.MutedPlayerModel;
import com.jeka8833.tntclientendpoints.services.discordbot.repositories.MutedPlayerRepository;
import com.jeka8833.tntclientendpoints.services.discordbot.service.commands.LiveChatService;
import com.jeka8833.tntclientendpoints.services.discordbot.service.commands.PlayerRequesterService;
import com.jeka8833.tntclientendpoints.services.discordbot.service.commands.PrivilegeChecker;
import com.jeka8833.tntclientendpoints.services.discordbot.service.mojang.MojangProfile;
import com.jeka8833.tntclientendpoints.services.discordbot.service.mojang.api.MojangApi;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MuteCommand implements SlashCommandEvent {
    private static final Map<Pattern, TimeUnit> regexToTimeUnit = Map.of(
            Pattern.compile("^(\\d+)s$"), TimeUnit.SECONDS,
            Pattern.compile("^(\\d+)m$"), TimeUnit.MINUTES,
            Pattern.compile("^(\\d+)h$"), TimeUnit.HOURS,
            Pattern.compile("^(\\d+)d$"), TimeUnit.DAYS
    );

    private final PrivilegeChecker privilegeChecker;
    private final MojangApi mojangAPI;
    private final PlayerRequesterService playerRequesterService;
    private final MutedPlayerRepository mutedPlayerRepository;
    private final LiveChatService liveChatService;

    @Override
    public CommandData getCommandData() {
        return Commands.slash("mute", "Controls the Mute list.").addSubcommands(

                new SubcommandData("add", "Add a player to the Mute list for TNTClient Chat.")
                        .addOptions(
                                new OptionData(OptionType.STRING, "player",
                                        "The player name or uuid to mute", true)

                                        .setRequiredLength(1, 36),

                                new OptionData(OptionType.STRING, "description",
                                        "The reason for the mute", true)

                                        .setRequiredLength(1, 512),

                                new OptionData(OptionType.STRING, "time",
                                        "The time for the mute, format: \"1d 2h 3m 4s\" it`s 1 day 2 hours " +
                                                "3 minutes 4 seconds", true)

                                        .setRequiredLength(2, 32)
                        ),

                new SubcommandData("remove", "Remove a player to the Mute list for TNTClient Chat.")

                        .addOptions(new OptionData(OptionType.STRING, "player",
                                "The player name or uuid to mute", true)

                                .setRequiredLength(1, 36)),

                new SubcommandData("list", "Mute list for TNTClient Chat.")

                        .addOptions(new OptionData(OptionType.INTEGER, "page",
                                "Page number", false)
                                .setMinValue(1))
        );
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event,
                                          @NotNull DeferReplyWrapper deferReplyWrapper) {
        if (privilegeChecker.hasNoAccess(event, deferReplyWrapper, "MUTE")) return;

        switch (event.getSubcommandName()) {
            case "add" -> add(event, deferReplyWrapper);
            case "remove" -> remove(event, deferReplyWrapper);
            case "list" -> list(event, deferReplyWrapper);

            case null, default -> deferReplyWrapper.replyError("Invalid command usage");
        }
    }

    private void add(@NotNull SlashCommandInteractionEvent event,
                     @NotNull DeferReplyWrapper deferReplyWrapper) {
        Optional<UUID> playerOpt =
                playerRequesterService.getProfileUuidOrReplay("player", event, deferReplyWrapper);
        if (playerOpt.isEmpty()) return;

        String descriptionString = event.getOption("description", "", OptionMapping::getAsString);
        String timeString = event.getOption("time", "", OptionMapping::getAsString);

        Duration duration = parseTime(timeString);
        if (duration == null) {
            deferReplyWrapper.replyError("Invalid time format.");

            return;
        }

        mutedPlayerRepository.save(new MutedPlayerModel(
                playerOpt.get(),
                event.getUser().getIdLong(),
                descriptionString,
                ZonedDateTime.now().plus(duration))
        );

        deferReplyWrapper.replyGood("Player muted");

        liveChatService.sendGlobalWarning(event.getUser(), "Muted player: " + playerOpt.get());
    }

    private void remove(@NotNull SlashCommandInteractionEvent event,
                        @NotNull DeferReplyWrapper deferReplyWrapper) {
        Optional<UUID> playerOpt =
                playerRequesterService.getProfileUuidOrReplay("player", event, deferReplyWrapper);
        if (playerOpt.isEmpty()) return;

        mutedPlayerRepository.deleteById(playerOpt.get());

        deferReplyWrapper.replyGood("Player " + playerOpt.get() + " unmuted");
    }

    private void list(@NotNull SlashCommandInteractionEvent event,
                      @NotNull DeferReplyWrapper deferReplyWrapper) {
        int pageNumber = event.getOption("page", 1, OptionMapping::getAsInt);

        Pageable pageable = PageRequest.of(pageNumber - 1, 10);
        Page<MutedPlayerModel> page = mutedPlayerRepository.findAll(pageable);


        EmbedBuilder builder = new EmbedBuilder()
                .setTitle("Muted Player list ")
                .setColor(Color.YELLOW);

        List<MutedPlayerModel> playerModels = page.getContent();
        if (playerModels.isEmpty()) {
            builder.setDescription("There is no more information, try reducing the page number.");
        } else {
            Set<UUID> playerUUIDs = playerModels.stream()
                    .map(MutedPlayerModel::getPlayer)
                    .collect(Collectors.toSet());
            Map<UUID, MojangProfile> profiles = mojangAPI.getProfiles(playerUUIDs);

            for (MutedPlayerModel model : playerModels) {
                MojangProfile profile = profiles.get(model.getPlayer());
                String header = profile.getNameAndUuidAsText() + " until: " + model.getUnmuteTime();
                String description = "Moderator: <@" + model.getModerator() + ">\nReason: " + model.getReason();

                builder.addField(header, description, false);
            }
        }

        builder.setFooter("Page " + pageNumber + " of " + page.getTotalPages());

        deferReplyWrapper.replyEmbeds(builder.build());
    }

    @Nullable
    @Contract(pure = true)
    private static Duration parseTime(@NotNull String time) {
        try {
            Duration duration = Duration.parse(time);

            if (duration.isNegative() || duration.isZero()) return null;

            return duration;
        } catch (Exception ignored) {
        }

        try {
            String[] args = time.strip().toLowerCase().split(" ");

            long seconds = 0;
            for (String arg : args) {
                for (Map.Entry<Pattern, TimeUnit> entry : regexToTimeUnit.entrySet()) {
                    if (entry.getKey().matcher(arg).matches()) {
                        seconds += entry.getValue().toSeconds(Long.parseLong(arg.substring(0, arg.length() - 1)));
                    }
                }
            }

            if (seconds <= 0) return null;

            return Duration.ofSeconds(seconds);
        } catch (Exception ignored) {
        }

        return null;
    }
}
