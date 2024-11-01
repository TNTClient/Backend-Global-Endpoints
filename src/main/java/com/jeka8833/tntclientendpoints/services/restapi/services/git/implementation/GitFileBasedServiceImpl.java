package com.jeka8833.tntclientendpoints.services.restapi.services.git.implementation;

import com.jeka8833.tntclientendpoints.services.restapi.services.git.ChangeFileTask;
import com.jeka8833.tntclientendpoints.services.restapi.services.git.GitService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

@Service
@ConditionalOnProperty(value = "tntclient.git", havingValue = "localfiles")
public class GitFileBasedServiceImpl implements GitService {
    private final Path gitFolder;

    public GitFileBasedServiceImpl(@Value("${tntclient.git.folder}") Path gitFolder) {
        this.gitFolder = gitFolder;
    }

    @Override
    public void addTask(ChangeFileTask... tasks) {
        for (ChangeFileTask task : tasks) {
            task.runAndCheckChanges();
        }
    }

    @Override
    public Path getGitFolder() {
        return gitFolder;
    }
}
