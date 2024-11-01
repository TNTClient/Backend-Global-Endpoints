package com.jeka8833.tntclientendpoints.services.restapi.services.git;

import java.nio.file.Path;

public interface GitService {
    void addTask(ChangeFileTask... task);

    Path getGitFolder();
}
