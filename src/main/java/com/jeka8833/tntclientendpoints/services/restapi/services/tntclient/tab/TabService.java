package com.jeka8833.tntclientendpoints.services.restapi.services.tntclient.tab;

import com.jeka8833.tntclientendpoints.services.restapi.dtos.git.GitAnimationConfigDto;
import com.jeka8833.tntclientendpoints.services.restapi.dtos.git.GitPlayerConfigDto;
import com.jeka8833.tntclientendpoints.services.restapi.dtos.web.PostTabDto;
import com.jeka8833.tntclientendpoints.services.restapi.services.git.ChangeFileTask;
import com.jeka8833.tntclientendpoints.services.restapi.services.git.GitService;
import com.jeka8833.tntclientendpoints.services.restapi.services.tntclient.PlayerConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.Path;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TabService {
    private final GitService gitService;
    private final TabFilterService tabFilterService;
    private final PlayerConfigService playerConfigService;

    public void updateTab(UUID player, PostTabDto tabDto) {
        if (tabFilterService.isValidAnimation(tabDto.tabAnimation())) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid animation text");
        }

        String[] animation = tabFilterService.normalizeAnimation(tabDto.tabAnimation());

        Path configPath = gitService.getGitFolder().resolve("player/config/" + player + ".json");
        gitService.addTask(
                new ChangeFileTask(configPath, () -> {
                    GitPlayerConfigDto playerGitConfig = playerConfigService.readOrDefault(configPath);

                    playerGitConfig.setAnimationConfig(new GitAnimationConfigDto(animation, tabDto.delayMs()));

                    playerConfigService.write(configPath, playerGitConfig);
                })
        );
    }

    public void removeTab(UUID player) {
        Path configPath = gitService.getGitFolder().resolve("player/config/" + player + ".json");
        gitService.addTask(
                new ChangeFileTask(configPath, () -> {
                    GitPlayerConfigDto playerGitConfig = playerConfigService.readOrDefault(configPath);

                    playerGitConfig.setAnimationConfig(null);

                    playerConfigService.write(configPath, playerGitConfig);
                })
        );
    }
}
