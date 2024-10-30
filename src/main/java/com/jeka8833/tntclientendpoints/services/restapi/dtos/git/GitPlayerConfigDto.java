package com.jeka8833.tntclientendpoints.services.restapi.dtos.git;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
public class GitPlayerConfigDto {
    public static final int MOJANG_CAPE_PRIORITY = 0;
    public static final int OPTIFINE_CAPE_PRIORITY = 1;
    public static final int TNTCLIENT_CAPE_PRIORITY = 2;

    private int capePriority;

    @Nullable
    private GitAnimationConfigDto animationConfig;
}
