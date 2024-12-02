package com.jeka8833.tntclientendpoints.services.restapi.dtos;

import jakarta.validation.constraints.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.ZonedDateTime;

public record SeasonalAccessoryDto(@NotNull ZonedDateTime start,
                                   @NotNull ZonedDateTime end,
                                   String @Nullable [] accessories) {
}
