package com.jeka8833.tntclientendpoints.services.restapi.services.tntclient.accessories;

import com.jeka8833.tntclientendpoints.services.restapi.dtos.AccessoryParameterDto;
import com.jeka8833.tntclientendpoints.services.restapi.dtos.git.GitPlayerConfigDto;
import com.jeka8833.tntclientendpoints.services.restapi.dtos.web.PostAccessoriesDto;
import com.jeka8833.tntclientendpoints.services.restapi.services.git.ChangeFileTask;
import com.jeka8833.tntclientendpoints.services.restapi.services.git.GitService;
import com.jeka8833.tntclientendpoints.services.restapi.services.tntclient.PlayerConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccessoriesService {
    private final GitService gitService;
    private final AccessoriesManager accessoriesManager;
    private final PlayerConfigService playerConfigService;

    public void updateAccessories(UUID player, PostAccessoriesDto accessoriesDto) {
        Map<String, AccessoryParameterDto> accessories = new HashMap<>(accessoriesDto.accessories().length);

        for (String accessory : accessoriesDto.accessories()) {
            AccessoryParameterDto parameterDto = accessoriesManager.getAccessory(accessory);
            if (parameterDto == null) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid accessory: " + accessory);
            }

            accessories.put(accessory, parameterDto);
        }

        Path configPath = gitService.getGitFolder().resolve("player/config/" + player + ".json");
        gitService.addTask(
                new ChangeFileTask(configPath, () -> {
                    GitPlayerConfigDto playerGitConfig = playerConfigService.readOrDefault(configPath);

                    playerGitConfig.setAccessories(accessories);

                    playerConfigService.write(configPath, playerGitConfig);
                })
        );
    }

    public void removeAccessories(UUID player) {
        Path configPath = gitService.getGitFolder().resolve("player/config/" + player + ".json");
        gitService.addTask(
                new ChangeFileTask(configPath, () -> {
                    GitPlayerConfigDto playerGitConfig = playerConfigService.readOrDefault(configPath);

                    playerGitConfig.setAccessories(new HashMap<>());

                    playerConfigService.write(configPath, playerGitConfig);
                })
        );
    }
}
