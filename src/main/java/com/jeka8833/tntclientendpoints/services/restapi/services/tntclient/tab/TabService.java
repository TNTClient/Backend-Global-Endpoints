package com.jeka8833.tntclientendpoints.services.restapi.services.tntclient.tab;

import com.jeka8833.tntclientendpoints.services.restapi.dtos.PlayerGitConfig;
import com.jeka8833.tntclientendpoints.services.restapi.dtos.PostTabDto;
import com.jeka8833.tntclientendpoints.services.restapi.services.git.ChangeFileTask;
import com.jeka8833.tntclientendpoints.services.restapi.services.git.GitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.Path;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TabService {
    private final TabFilterService tabFilterService;
    private final GitService gitService;

    public void updateTab(UUID player, PostTabDto tabDto) {
        if (tabFilterService.isValidAnimation(tabDto.tabAnimation())) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid animation text");
        }

        String[] animation = tabFilterService.normalizeAnimation(tabDto.tabAnimation());

        Path configPath = gitService.getGitFolder().resolve("player/config/" + player + ".json");
        gitService.addTask(
                new ChangeFileTask(configPath, () -> {
                    PlayerGitConfig playerGitConfig = PlayerGitConfig.readOrDefault(configPath);

                    playerGitConfig.setAnimationConfig(
                            new PlayerGitConfig.AnimationConfig(animation, tabDto.delayMs()));

                    playerGitConfig.write(configPath);
                })
        );
    }

    public void removeTab(UUID player) {
        Path configPath = gitService.getGitFolder().resolve("player/config/" + player + ".json");
        gitService.addTask(
                new ChangeFileTask(configPath, () -> {
                    PlayerGitConfig playerGitConfig = PlayerGitConfig.readOrDefault(configPath);
                    playerGitConfig.setAnimationConfig(null);

                    playerGitConfig.write(configPath);
                })
        );
    }
}
