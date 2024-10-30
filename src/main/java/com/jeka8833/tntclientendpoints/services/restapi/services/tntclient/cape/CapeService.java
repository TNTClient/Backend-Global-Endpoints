package com.jeka8833.tntclientendpoints.services.restapi.services.tntclient.cape;

import com.jeka8833.tntclientendpoints.services.restapi.dtos.git.GitPlayerConfigDto;
import com.jeka8833.tntclientendpoints.services.restapi.dtos.web.PostCapeDto;
import com.jeka8833.tntclientendpoints.services.restapi.services.git.ChangeFileTask;
import com.jeka8833.tntclientendpoints.services.restapi.services.git.GitService;
import com.jeka8833.tntclientendpoints.services.restapi.services.git.PlayerConfigService;
import com.jeka8833.tntclientendpoints.services.restapi.services.nsfwChecker.NsfwResult;
import com.jeka8833.tntclientendpoints.services.restapi.services.nsfwChecker.NsfwScannerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Slf4j
@Service
public class CapeService {
    private final CapeFilterService capeFilterService;
    private final NsfwScannerService nsfwScannerService;
    private final PlayerConfigService playerConfigService;
    private final GitService gitService;

    private final int maxFileSize;
    private final String[] allowedImageFormats;
    private final int maxImageWidth;
    private final int maxImageHeight;

    public CapeService(CapeFilterService capeFilterService, NsfwScannerService nsfwScannerService,
                       GitService gitService, PlayerConfigService playerConfigService,
                       @Value("${tntclient.cape.parse.maxfilesize: 10MB}") DataSize maxFileSize,
                       @Value("${tntclient.cape.parse.allowedimageformats: png,jpeg}") String[] allowedImageFormats,
                       @Value("${tntclient.cape.parse.maximagewidth: #{2048}}") int maxImageWidth,
                       @Value("${tntclient.cape.parse.maximageheight: #{1024}}") int maxImageHeight) {
        this.capeFilterService = capeFilterService;
        this.nsfwScannerService = nsfwScannerService;
        this.gitService = gitService;
        this.playerConfigService = playerConfigService;
        this.maxFileSize = Math.toIntExact(maxFileSize.toBytes());
        this.allowedImageFormats = allowedImageFormats;
        this.maxImageWidth = maxImageWidth;
        this.maxImageHeight = maxImageHeight;

        if (this.maxFileSize < 8) throw new IllegalArgumentException("Invalid maxFileSize < 8");
        if (this.allowedImageFormats.length < 1) {
            throw new IllegalArgumentException("Invalid allowedImageFormats.length < 1");
        }
        if (this.maxImageWidth < 64) throw new IllegalArgumentException("Invalid maxImageWidth < 64");
        if (this.maxImageHeight < 32) throw new IllegalArgumentException("Invalid maxImageHeight < 32");
    }

    public void updateCape(UUID player, PostCapeDto capeDto) {
        try {
            if (capeDto.data().isEmpty()) {
                changeOnlyVisibility(player, capeDto.enabled());
            } else {
                try {
                    changeCapeAndVisibility(player, capeDto.data().get(), capeDto.enabled());
                } catch (Exception e) {
                    changeOnlyVisibility(player, capeDto.enabled());

                    throw e;
                }
            }
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage());
        } catch (Exception e) {
            log.warn("Failed to load cape for: {}", player, e);

            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public void removeCape(UUID player) {
        Path capePath = gitService.getGitFolder().resolve("player/cape/" + player + ".png");
        Path configPath = gitService.getGitFolder().resolve("player/config/" + player + ".json");

        gitService.addTask(
                new ChangeFileTask(capePath, () -> Files.deleteIfExists(capePath)),
                new ChangeFileTask(configPath, () -> {
                    GitPlayerConfigDto playerGitConfig = playerConfigService.readOrDefault(configPath);

                    if (playerGitConfig.getCapePriority() == GitPlayerConfigDto.TNTCLIENT_CAPE_PRIORITY) {
                        playerGitConfig.setCapePriority(GitPlayerConfigDto.OPTIFINE_CAPE_PRIORITY);
                    }

                    playerConfigService.write(configPath, playerGitConfig);
                })
        );
    }

    private void changeCapeAndVisibility(UUID player, String dataStream, boolean visible) throws Exception {
        byte[] imageByteArray = capeFilterService.loadTNTClientResource(dataStream, maxFileSize);

        if (!capeFilterService.isValidImageParameters(
                imageByteArray, allowedImageFormats, maxImageWidth, maxImageHeight)) {
            throw new IllegalArgumentException("Image format not allowed");
        }

        byte[] finalImageByteArray = capeFilterService.repackPlayerCape(imageByteArray);

        if (nsfwScannerService.scan(finalImageByteArray) == NsfwResult.UNSAFE) {
            throw new IllegalArgumentException("Image is NSFW");
        }

        Path capePath = gitService.getGitFolder().resolve("player/cape/" + player + ".png");
        Path configPath = gitService.getGitFolder().resolve("player/config/" + player + ".json");

        gitService.addTask(
                new ChangeFileTask(capePath, () -> Files.write(capePath, finalImageByteArray)),
                new ChangeFileTask(configPath, () -> {
                    GitPlayerConfigDto playerGitConfig = playerConfigService.readOrDefault(configPath);
                    playerGitConfig.setCapePriority(visible ?
                            GitPlayerConfigDto.TNTCLIENT_CAPE_PRIORITY : GitPlayerConfigDto.OPTIFINE_CAPE_PRIORITY);

                    playerConfigService.write(configPath, playerGitConfig);
                })
        );
    }

    private void changeOnlyVisibility(UUID player, boolean visible) {
        if (visible) {
            Path capePath = gitService.getGitFolder().resolve("player/cape/" + player + ".png");

            if (!Files.exists(capePath)) {
                throw new IllegalArgumentException("Can't change visibility without cape");
            }
        }

        Path configPath = gitService.getGitFolder().resolve("player/config/" + player + ".json");

        gitService.addTask(
                new ChangeFileTask(configPath, () -> {
                    GitPlayerConfigDto playerGitConfig = playerConfigService.readOrDefault(configPath);
                    playerGitConfig.setCapePriority(visible ?
                            GitPlayerConfigDto.TNTCLIENT_CAPE_PRIORITY : GitPlayerConfigDto.OPTIFINE_CAPE_PRIORITY);

                    playerConfigService.write(configPath, playerGitConfig);
                })
        );
    }
}
