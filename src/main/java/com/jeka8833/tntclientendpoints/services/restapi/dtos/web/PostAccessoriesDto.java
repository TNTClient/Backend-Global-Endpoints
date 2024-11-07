package com.jeka8833.tntclientendpoints.services.restapi.dtos.web;

import jakarta.validation.constraints.NotNull;

public record PostAccessoriesDto(@NotNull String @NotNull [] accessories) {
}
