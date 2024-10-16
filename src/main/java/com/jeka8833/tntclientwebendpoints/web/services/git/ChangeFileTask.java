package com.jeka8833.tntclientwebendpoints.web.services.git;

import org.apache.commons.codec.digest.DigestUtils;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public record ChangeFileTask(Path path, GitTask task) {
    public boolean runAndCheckChanges() {
        try {
            byte[] hash = generateHashSilent(path);
            task.run();
            return !Arrays.equals(hash, generateHashSilent(path));
        } catch (Exception e) {
            return true;
        }
    }

    private static byte @Nullable [] generateHashSilent(Path filePath) {
        try (InputStream inputStream = Files.newInputStream(filePath)) {
            return DigestUtils.md5(inputStream);
        } catch (Exception e) {
            return null;
        }
    }
}
