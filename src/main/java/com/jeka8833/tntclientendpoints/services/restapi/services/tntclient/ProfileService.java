package com.jeka8833.tntclientendpoints.services.restapi.services.tntclient;

import com.jeka8833.tntclientendpoints.services.restapi.services.git.ChangeFileTask;
import com.jeka8833.tntclientendpoints.services.restapi.services.git.GitService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileService {
    private final GitService gitService;

    public void deleteProfile(UUID player) {
        Path capePath = gitService.getGitFolder().resolve("player/cape/" + player + ".png");
        Path configPath = gitService.getGitFolder().resolve("player/config/" + player + ".json");

        gitService.addTask(
                new ChangeFileTask(capePath, () -> Files.deleteIfExists(capePath)),
                new ChangeFileTask(configPath, () -> Files.deleteIfExists(configPath))
        );
    }
}
