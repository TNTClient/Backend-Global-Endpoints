package com.jeka8833.tntclientwebendpoints.web.services.git;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
@Service
public class GitService {
    private final BlockingQueue<ChangeFileTask> tasks = new LinkedBlockingQueue<>();

    @Getter
    private final Path gitFolder;
    private final String repositoryUrl;

    private Git git;

    public GitService(@Value("${tntclient.git.folder}") Path gitFolder,
                      @Value("${tntclient.git.url}") String repositoryUrl) throws GitAPIException, IOException {
        this.gitFolder = gitFolder;
        this.repositoryUrl = repositoryUrl;

        git = cloneRepository(gitFolder, repositoryUrl);
    }

    public void addTask(ChangeFileTask... task) {
        tasks.addAll(Arrays.asList(task));
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        Thread.startVirtualThread(() -> {
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
                    }
                } catch (Exception e) {
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

    private void pullOrClone() throws GitAPIException, IOException {
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

    private static Git cloneRepository(Path gitFolder, String repositoryUrl) throws GitAPIException, IOException {
        FileSystemUtils.deleteRecursively(gitFolder);

        return Git.cloneRepository()
                .setGitDir(gitFolder.toFile())
                .setURI(repositoryUrl)
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
}
