package com.jeka8833.tntclientwebendpoints.web.services.tntclient.cape;

import com.jeka8833.tntclientwebendpoints.web.dtos.PlayerGitConfig;
import com.jeka8833.tntclientwebendpoints.web.dtos.PostCapeDto;
import com.jeka8833.tntclientwebendpoints.web.services.git.ChangeFileTask;
import com.jeka8833.tntclientwebendpoints.web.services.git.GitService;
import com.jeka8833.tntclientwebendpoints.web.services.nsfwChecker.NsfwResult;
import com.jeka8833.tntclientwebendpoints.web.services.nsfwChecker.NsfwScannerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Slf4j
@Service
public class CapeService {
    private final CapeFilterService capeFilterService;
    private final NsfwScannerService nsfwScannerService;
    private final GitService gitService;

    private final int maxFileSize;
    private final String[] allowedImageFormats;
    private final int maxImageWidth;
    private final int maxImageHeight;

    public CapeService(CapeFilterService capeFilterService, NsfwScannerService nsfwScannerService, GitService gitService,
                       @Value("${tntclient.cape.parse.maxfilesize: #{10485760}}") int maxFileSize,
                       @Value("${tntclient.cape.parse.allowedimageformats: png,jpeg}") String[] allowedImageFormats,
                       @Value("${tntclient.cape.parse.maximagewidth: #{2048}}") int maxImageWidth,
                       @Value("${tntclient.cape.parse.maximageheight: #{1024}}") int maxImageHeight) {
        this.capeFilterService = capeFilterService;
        this.nsfwScannerService = nsfwScannerService;
        this.gitService = gitService;
        this.maxFileSize = maxFileSize;
        this.allowedImageFormats = allowedImageFormats;
        this.maxImageWidth = maxImageWidth;
        this.maxImageHeight = maxImageHeight;

        if (maxFileSize < 8) throw new IllegalArgumentException("Invalid maxFileSize < 8");
        if (allowedImageFormats.length < 1) {
            throw new IllegalArgumentException("Invalid allowedImageFormats.length < 1");
        }
        if (maxImageWidth < 64) throw new IllegalArgumentException("Invalid maxImageWidth < 64");
        if (maxImageHeight < 32) throw new IllegalArgumentException("Invalid maxImageHeight < 32");

    }

    public void updateCape(UUID player, PostCapeDto capeDto) {
        try {
            byte[] imageByteArray = capeFilterService.loadTNTClientResource(capeDto.data(), maxFileSize);

            if (!capeFilterService.isValidImageParameters(
                    imageByteArray, allowedImageFormats, maxImageWidth, maxImageHeight)) {
                throw new IllegalArgumentException("Image format not allowed");
            }

            imageByteArray = capeFilterService.repackPlayerCape(imageByteArray);

            if (nsfwScannerService.scan(imageByteArray) == NsfwResult.UNSAFE) {
                throw new IllegalArgumentException("Image is NSFW");
            }

            Path capePath = gitService.getGitFolder().resolve("player/cape/" + player + ".png");
            Path configPath = gitService.getGitFolder().resolve("player/config/" + player + ".json");

            byte[] finalImageByteArray = imageByteArray;
            gitService.addTask(
                    new ChangeFileTask(capePath, () -> Files.write(capePath, finalImageByteArray)),
                    new ChangeFileTask(configPath, () -> {
                        PlayerGitConfig playerGitConfig = PlayerGitConfig.readOrDefault(configPath);
                        playerGitConfig.setCapePriority(capeDto.enabled() ?
                                PlayerGitConfig.TNTCLIENT_CAPE_PRIORITY : PlayerGitConfig.OPTIFINE_CAPE_PRIORITY);

                        playerGitConfig.write(configPath);
                    })
            );
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
                    PlayerGitConfig playerGitConfig = PlayerGitConfig.readOrDefault(configPath);

                    if (playerGitConfig.getCapePriority() == PlayerGitConfig.TNTCLIENT_CAPE_PRIORITY) {
                        playerGitConfig.setCapePriority(PlayerGitConfig.OPTIFINE_CAPE_PRIORITY);
                    }

                    playerGitConfig.write(configPath);
                })
        );
    }
}
