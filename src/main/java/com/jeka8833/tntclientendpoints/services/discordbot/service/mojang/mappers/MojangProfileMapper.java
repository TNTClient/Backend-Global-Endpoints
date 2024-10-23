package com.jeka8833.tntclientendpoints.services.discordbot.service.mojang.mappers;

import com.jeka8833.tntclientendpoints.services.discordbot.service.mojang.MojangProfile;
import com.jeka8833.tntclientendpoints.services.discordbot.service.mojang.dtos.MojangProfileDto;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.UUID;

@Slf4j
@Mapper(componentModel = "spring")
public abstract class MojangProfileMapper {
    @Mapping(target = "uuid", source = "id", qualifiedByName = "toUUID")
    public abstract MojangProfile toMojangProfile(MojangProfileDto dto);

    @Named("toUUID")
    static UUID toUUID(String value) {
        if (value == null) return null;

        try {
            return UUID.fromString(
                    value.replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));
        } catch (Exception e) {
            log.warn("Failed to parse UUID: {}", value, e);
        }

        return null;
    }
}
