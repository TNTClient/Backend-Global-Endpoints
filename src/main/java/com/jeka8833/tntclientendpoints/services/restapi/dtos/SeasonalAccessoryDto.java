package com.jeka8833.tntclientendpoints.services.restapi.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.ZonedDateTime;

public record SeasonalAccessoryDto(@JsonFormat(pattern = "yyyy-MM-dd@HH:mm:ss.SSSZ") @NotNull ZonedDateTime start,
                                   @JsonFormat(pattern = "yyyy-MM-dd@HH:mm:ss.SSSZ") @NotNull ZonedDateTime end,
                                   String @Nullable [] accessories) {
}
