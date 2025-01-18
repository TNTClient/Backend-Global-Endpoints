package com.jeka8833.tntclientendpoints.services.restapi.services.git.implementation.remote;

import com.jeka8833.tntclientendpoints.services.restapi.services.git.ChangeFileTask;
import com.jeka8833.tntclientendpoints.services.restapi.services.git.GitService;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
@Service
@ConditionalOnProperty(value = "tntclient.git", havingValue = "remotegit", matchIfMissing = true)
public class GitServiceImpl implements GitService {
    private final BlockingQueue<ChangeFileTask> tasks = new LinkedBlockingQueue<>();

    private final Path gitFolder;
    private final AsyncTaskExecutor executor;
    private final String repositoryUrl;
    private final GitBackupService gitBackupService;

    private Git git;

    public GitServiceImpl(@Value("${tntclient.git.folder}") Path gitFolder,
                          @Qualifier("virtual-executor") AsyncTaskExecutor executor,
                          @Value("${tntclient.git.url}") String repositoryUrl,
                          GitBackupService gitBackupService) throws GitAPIException {
        this.gitFolder = gitFolder;
        this.executor = executor;
        this.repositoryUrl = repositoryUrl;
        this.gitBackupService = gitBackupService;

        git = cloneRepository(gitFolder, repositoryUrl);
    }

    @Override
    public void addTask(ChangeFileTask... task) {
        tasks.addAll(Arrays.asList(task));
    }

    @Override
    public Path getGitFolder() {
        return gitFolder;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        executor.execute(() -> {
            while (!Thread.interrupted()) {
                Collection<String> filesChangedNames = new ArrayList<>();

                Collection<ChangeFileTask> backup = new ArrayList<>();

                try {
                    ChangeFileTask task = tasks.take();
                    backup.add(task);

                    pullOrClone();

                    boolean filesChanged = task.runAndCheckChanges();
                    if (filesChanged) {
                        filesChangedNames.add(task.path().getFileName().toString());
                    }

                    while ((task = tasks.poll()) != null) {
                        boolean isChanged = task.runAndCheckChanges();
                        if (isChanged) {
                            filesChangedNames.add(task.path().getFileName().toString());
                        }

                        filesChanged |= isChanged;
                    }

                    if (filesChanged) {
                        pushFiles(git, generateCommitName(filesChangedNames));
                        gitBackupService.tryBackup();
                    }
                } catch (Exception e) {
                    log.warn("Fail to push files, try redownload repository", e);

                    try {
                        git = cloneRepository(gitFolder, repositoryUrl);

                        for (ChangeFileTask task : backup) {
                            try {
                                pullOrClone();
                                if (!task.runAndCheckChanges()) continue;

                                pushFiles(git, generateCommitName(
                                        Collections.singletonList(task.path().getFileName().toString())));
                            } catch (Exception ex) {
                                log.warn("Fail to pull one file", e);
                            }
                        }
                    } catch (Exception ex) {
                        log.warn("Fail to push files", e);
                    }
                }
            }
        });
    }

    @PreDestroy
    public void onExit() {
        if (git != null) {
            git.close();
        }
    }

    private void pullOrClone() throws GitAPIException {
        if (pullFiles(git)) return;

        git = cloneRepository(gitFolder, repositoryUrl);
    }

    private static String generateCommitName(Iterable<String> filesChangedNames) {
        StringBuilder stringBuffer = new StringBuilder();

        for (String file : filesChangedNames) {
            stringBuffer.append("Changed: ").append(file).append('\n');
        }

        return stringBuffer.toString();
    }

    private static Git cloneRepository(Path gitFolder, String repositoryUrl) throws GitAPIException {
        deleteAllFiles(gitFolder);

        return Git.cloneRepository()
                .setURI(repositoryUrl)
                .setDirectory(gitFolder.toFile())
                .call();
    }

    private static boolean pullFiles(Git git) {
        try {
            git.pull()
                    .setRebase(true)
                    .call();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static void pushFiles(Git git, String message) throws GitAPIException {
        git.checkout()
                .setName("latest_branch").setOrphan(true).setForced(true).call();
        git.add()
                .addFilepattern(".").call();
        git.commit()
                .setMessage(message).call();
        git.branchDelete()
                .setBranchNames("main").setForce(true).call();
        git.branchRename()
                .setOldName("latest_branch").setNewName("main").call();
        git.push()
                .setForce(true).call();
    }

    private static void deleteAllFiles(Path path) {
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<>() {
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    try {
                        Files.delete(file);
                    } catch (Exception e) {
                        try {
                            Files.setAttribute(file, "dos:readonly", false);

                            Files.delete(file);
                        } catch (Exception e1) {
                            log.warn("Fail to delete file", e);
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }

                public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                    try {
                        Files.delete(dir);
                    } catch (IOException e) {
                        log.warn("Fail to delete directory", e);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            log.warn("Fail to delete all files", e);
        }
    }
}
