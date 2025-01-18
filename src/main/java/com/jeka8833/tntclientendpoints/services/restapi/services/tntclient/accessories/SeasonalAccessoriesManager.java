package com.jeka8833.tntclientendpoints.services.restapi.services.tntclient.accessories;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeka8833.tntclientendpoints.services.general.tntclintapi.database.UserRole;
import com.jeka8833.tntclientendpoints.services.general.util.UuidUtil;
import com.jeka8833.tntclientendpoints.services.restapi.dtos.AccessoryParameterDto;
import com.jeka8833.tntclientendpoints.services.restapi.dtos.SeasonalAccessoryDto;
import com.jeka8833.tntclientendpoints.services.restapi.dtos.git.GitPlayerConfigDto;
import com.jeka8833.tntclientendpoints.services.restapi.models.TNTClientUser;
import com.jeka8833.tntclientendpoints.services.restapi.repositories.UserRepository;
import com.jeka8833.tntclientendpoints.services.restapi.services.git.ChangeFileTask;
import com.jeka8833.tntclientendpoints.services.restapi.services.git.GitService;
import com.jeka8833.tntclientendpoints.services.restapi.services.tntclient.PlayerConfigService;
import lombok.Locked;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.hibernate.annotations.Immutable;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class SeasonalAccessoriesManager {
    private final Request httpRequest;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final GitService gitService;
    private final PlayerConfigService playerConfigService;
    private final ScheduledExecutorService scheduledExecutorService;

    private final Collection<ScheduledFuture<?>> planedSchedules = new ArrayList<>();
    private SeasonalAccessoryDto @NotNull [] seasonalList;

    private SeasonalAccessoriesManager(@Value("${tntclient.accessories.seasonal.url}") URL accessoriesListUrl,
                                       ScheduledExecutorService scheduledExecutorService,
                                       OkHttpClient httpClient, ObjectMapper objectMapper,
                                       UserRepository userRepository, GitService gitService,
                                       PlayerConfigService playerConfigService) throws IOException {
        this.scheduledExecutorService = scheduledExecutorService;
        this.playerConfigService = playerConfigService;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
        this.httpClient = httpClient;
        this.gitService = gitService;

        this.httpRequest = new Request.Builder().url(accessoriesListUrl).build();

        if (!tryReloadSeasonalList()) throw new RuntimeException("Can't reload seasonal accessories");
    }

    @NotNull
    public Set<@NotNull String> getActiveSeasonalAccessory() throws RuntimeException {
        Set<String> out = new HashSet<>();

        ZonedDateTime currentTime = ZonedDateTime.now();
        for (SeasonalAccessoryDto seasonal : seasonalList) {
            if (currentTime.isAfter(seasonal.start()) && currentTime.isBefore(seasonal.end())) {
                String[] accessories = seasonal.accessories();
                if (accessories == null) continue;

                out.addAll(Arrays.asList(accessories));
            }
        }

        return out;
    }

    public boolean tryReloadSeasonalList() {
        try {
            seasonalList = forceReadSeasonalList();

            removeAllSeasonalAccessories();

            updateScheduled(seasonalList);

            return true;
        } catch (Exception e) {
            log.warn("Fail to reload seasonal list", e);

            return false;
        }
    }

    @Locked
    private void updateScheduled(SeasonalAccessoryDto @NotNull [] seasonalList) {
        for (ScheduledFuture<?> scheduledFuture : planedSchedules) {
            scheduledFuture.cancel(true);
        }

        planedSchedules.clear();

        ZonedDateTime startTime = ZonedDateTime.now().minusMinutes(5);  // Add 5 Minutes before remove accessories

        for (SeasonalAccessoryDto seasonal : seasonalList) {
            long until = startTime.until(seasonal.end(), ChronoUnit.NANOS);

            if (until > 0) {
                log.info("Seasonal accessory {} reset at: {} minutes",
                        seasonal.accessories(), TimeUnit.NANOSECONDS.toMinutes(until));

                ScheduledFuture<?> schedule = scheduledExecutorService.schedule(
                        this::removeAllSeasonalAccessories, until, TimeUnit.NANOSECONDS);

                planedSchedules.add(schedule);
            }
        }
    }

    private @NotNull SeasonalAccessoryDto @NotNull [] forceReadSeasonalList() throws IOException {
        try (Response response = httpClient.newCall(httpRequest).execute()) {
            if (!response.isSuccessful()) throw new RuntimeException("Response code: " + response.code());

            try (ResponseBody responseBody = response.body();
                 InputStream inputStream = responseBody.byteStream()) {
                SeasonalAccessoryDto[] value = objectMapper.readValue(inputStream, SeasonalAccessoryDto[].class);
                if (value == null) throw new RuntimeException("Fail to parse accessories list");

                return value;
            }
        }
    }

    public void removeAllSeasonalAccessories() {
        Set<String> seasonalAccessories = getActiveSeasonalAccessory();

        Collection<ChangeFileTask> tasks = getNeedToDeletePlayers().stream()
                .map(playerUUID -> {
                    Path configPath = gitService.getGitFolder().resolve("player/config/" + playerUUID + ".json");

                    return new ChangeFileTask(configPath, () -> {
                        GitPlayerConfigDto playerGitConfig = playerConfigService.readOrDefault(configPath);

                        boolean needUpdate = false;
                        Map<String, AccessoryParameterDto> allowed = new HashMap<>();
                        for (Map.Entry<String, AccessoryParameterDto> accesoryEntry :
                                playerGitConfig.getAccessories().entrySet()) {
                            if (seasonalAccessories.contains(accesoryEntry.getKey())) {
                                allowed.put(accesoryEntry.getKey(), accesoryEntry.getValue());
                            } else {
                                needUpdate = true;
                            }
                        }

                        if (needUpdate) {
                            playerGitConfig.setAccessories(allowed);

                            playerConfigService.write(configPath, playerGitConfig);
                        }
                    });
                })
                .toList();

        gitService.addTask(tasks.toArray(new ChangeFileTask[0]));
    }

    @NotNull
    private Collection<@NotNull UUID> getNeedToDeletePlayers() {
        Collection<UUID> allUsers = getAllUsers();
        Iterable<TNTClientUser> tntClientUsers = userRepository.findAllById(allUsers);

        Collection<UUID> needToDelete = new HashSet<>(allUsers);
        for (TNTClientUser user : tntClientUsers) {
            if (user.getRoles() != null && AuthorityUtils.commaSeparatedStringToAuthorityList(
                    user.getRoles()).contains(UserRole.HAS_ACCESSORIES)) {
                needToDelete.remove(user.getUser());
            }
        }

        return needToDelete;
    }

    @NotNull
    @Immutable
    private Collection<@NotNull UUID> getAllUsers() {
        Path configFolder = gitService.getGitFolder().resolve("player/config/");

        try (Stream<Path> stream = Files.walk(configFolder)) {
            return stream
                    .map(path -> path.getFileName().toString())
                    .filter(fileName -> fileName.endsWith(".json"))
                    .map(fileName -> UuidUtil.parseOrNull(fileName.substring(0, fileName.length() - 5)))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            log.warn("Fail to get all users", e);

            return Collections.emptyList();
        }
    }
}
