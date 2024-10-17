package com.jeka8833.tntclientendpoints.services.restapi.services.git;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public final class GitBackupService {
    private final Path gitFolder;
    private final Path backupFolder;
    private final long backupIntervalNanos;

    private long lastBackupTimeNanos = System.nanoTime();

    GitBackupService(@Value("${tntclient.git.folder}") Path gitFolder,
                     @Value("${tntclient.git.backup.folder}") Path backupFolder,
                     @Value("${tntclient.git.backup.intervalmin}") long backupIntervalMinutes) {
        this.gitFolder = gitFolder;
        this.backupFolder = backupFolder;
        this.backupIntervalNanos = TimeUnit.MINUTES.toNanos(backupIntervalMinutes);
    }

    public void tryBackup() {
        if (System.nanoTime() - lastBackupTimeNanos < backupIntervalNanos) return;

        try {
            FileSystemUtils.copyRecursively(gitFolder, backupFolder);

            lastBackupTimeNanos = System.nanoTime();
        } catch (IOException e) {
            log.warn("Fail to backup git", e);
        }
    }
}
