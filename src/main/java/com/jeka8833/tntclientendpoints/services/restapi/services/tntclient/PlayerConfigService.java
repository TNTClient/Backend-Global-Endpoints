package com.jeka8833.tntclientendpoints.services.restapi.services.tntclient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeka8833.tntclientendpoints.services.restapi.dtos.git.GitPlayerConfigDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlayerConfigService {
    private final ObjectMapper objectMapper;

    public void write(Path path, GitPlayerConfigDto config) throws IOException {
        if (config.getCapePriority() == GitPlayerConfigDto.TNTCLIENT_CAPE_PRIORITY ||
                config.getAnimationConfig() != null || !config.getAccessories().isEmpty()) {

            try (OutputStream outputStream = Files.newOutputStream(path)) {
                objectMapper.writeValue(outputStream, config);
            }

            log.info("Updated player config file: {}", path);
        } else {
            Files.deleteIfExists(path);
            log.info("Deleted empty player config file: {}", path);
        }
    }

    public GitPlayerConfigDto readOrDefault(Path path) {
        try {
            return read(path);
        } catch (Exception e) {
            return new GitPlayerConfigDto();
        }
    }

    public GitPlayerConfigDto read(Path path) throws IOException {
        try (InputStream inputStream = Files.newInputStream(path)) {
            return objectMapper.readValue(inputStream, GitPlayerConfigDto.class);
        }
    }
}
