package com.jeka8833.tntclientendpoints.services.discordbot.commands;

import com.jeka8833.tntclientendpoints.services.discordbot.DeferReplyWrapper;
import com.jeka8833.tntclientendpoints.services.discordbot.listeners.SlashCommand;
import com.jeka8833.tntclientendpoints.services.discordbot.service.PrivilegeChecker;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.boot.convert.DurationStyle;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class MuteCommand implements SlashCommand {
    private static final Map<Pattern, TimeUnit> regexToTimeUnit = Map.of(
            Pattern.compile("^(\\d+)s$"), TimeUnit.SECONDS,
            Pattern.compile("^(\\d+)m$"), TimeUnit.MINUTES,
            Pattern.compile("^(\\d+)h$"), TimeUnit.HOURS,
            Pattern.compile("^(\\d+)d$"), TimeUnit.DAYS
    );

    private final PrivilegeChecker privilegeChecker;

    @Override
    public CommandData getCommandData() {
        return Commands.slash("mute", "Controls the Mute list.").addSubcommands(

                new SubcommandData("add", "Add a player to the Mute list for TNTClient Chat.")

                        .addOption(OptionType.STRING, "player",
                                "The player name or uuid to mute", true)

                        .addOption(OptionType.STRING, "description",
                                "The reason for the mute", true)

                        .addOption(OptionType.STRING, "time", "The time for the mute, format: " +
                                "\"1d 2h 3m 4s\" it`s 1 day 2 hours 3 minutes 4 seconds", true),

                new SubcommandData("remove", "Remove a player to the Mute list for TNTClient Chat.")

                        .addOption(OptionType.STRING, "player",
                                "The player name or uuid to mute", true),

                new SubcommandData("list", "Mute list for TNTClient Chat."));
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event,
                                          @NotNull DeferReplyWrapper deferReplay) {
        if (privilegeChecker.hasNoAccess(event, deferReplay, "MUTE")) return;

        switch (event.getSubcommandName()) {
            case "add" -> add(event, deferReplay);
            case "remove" -> remove(event, deferReplay);
            case "list" -> list(event, deferReplay);

            case null, default -> deferReplay.replyError("Invalid command usage");
        }
    }

    private static void add(@NotNull SlashCommandInteractionEvent event,
                            @NotNull DeferReplyWrapper deferReplay) {
        String player = event.getOption("player").getAsString();
        String description = event.getOption("description").getAsString();
        String time = event.getOption("time").getAsString();

        deferReplay.replyGood(DurationStyle.SIMPLE.parse(time).toString());
    }

    private static void remove(@NotNull SlashCommandInteractionEvent event,
                               @NotNull DeferReplyWrapper deferReplay) {
        String player = event.getOption("player").getAsString();


        deferReplay.replyError("TODO");
    }

    private static void list(@NotNull SlashCommandInteractionEvent event,
                             @NotNull DeferReplyWrapper deferReplay) {

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
