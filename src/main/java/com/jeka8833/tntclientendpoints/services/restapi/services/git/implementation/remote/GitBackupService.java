package com.jeka8833.tntclientendpoints.services.restapi.services.git.implementation.remote;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;

@Slf4j
@Service
public final class GitBackupService {
    private final Path gitFolder;
    private final Path backupFolder;
    private final long backupIntervalNanos;

    private long lastBackupTimeNanos = System.nanoTime();

    GitBackupService(@Value("${tntclient.git.folder}") Path gitFolder,
                     @Value("${tntclient.git.backup.folder}") Path backupFolder,
                     @Value("${tntclient.git.backup.interval}") Duration backupInterval) {
        this.gitFolder = gitFolder;
        this.backupFolder = backupFolder;
        this.backupIntervalNanos = backupInterval.toNanos();
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
