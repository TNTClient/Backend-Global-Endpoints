package com.jeka8833.tntclientendpoints.services.restapi.dtos.git;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.jeka8833.tntclientendpoints.services.restapi.dtos.AccessoryParameterDto;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class GitPlayerConfigDto {
    public static final int MOJANG_CAPE_PRIORITY = 0;
    public static final int OPTIFINE_CAPE_PRIORITY = 1;
    public static final int TNTCLIENT_CAPE_PRIORITY = 2;

    private int capePriority = OPTIFINE_CAPE_PRIORITY;

    @Nullable
    private GitAnimationConfigDto animationConfig;

    private Map<String, AccessoryParameterDto> accessories = new HashMap<>();
}
