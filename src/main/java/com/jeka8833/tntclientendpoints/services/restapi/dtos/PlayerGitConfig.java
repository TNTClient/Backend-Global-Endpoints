package com.jeka8833.tntclientendpoints.services.restapi.dtos;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Getter
@Setter
public class PlayerGitConfig {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static final int MOJANG_CAPE_PRIORITY = 0;
    public static final int OPTIFINE_CAPE_PRIORITY = 1;
    public static final int TNTCLIENT_CAPE_PRIORITY = 2;

    private int capePriority;
    private @Nullable AnimationConfig animationConfig;

    public void write(Path path) throws IOException {
        try (OutputStream inputStream = Files.newOutputStream(path)) {
            OBJECT_MAPPER.writeValue(inputStream, this);
        }
    }

    public static PlayerGitConfig read(Path path) throws IOException {
        try (InputStream inputStream = Files.newInputStream(path)) {
            return OBJECT_MAPPER.readValue(inputStream, PlayerGitConfig.class);
        }
    }

    public static PlayerGitConfig readOrDefault(Path path) {
        try {
            return read(path);
        } catch (Exception e) {
            return new PlayerGitConfig();
        }
    }

    public record AnimationConfig(String[] textAnimation, int timeShift) {
    }
}
