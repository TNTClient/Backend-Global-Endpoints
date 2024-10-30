package com.jeka8833.tntclientendpoints.services.restapi.dtos.web;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import org.jetbrains.annotations.Nullable;

public record PostTabDto(@Nullable String @Size(min = 1, max = 16) [] tabAnimation,
                         @Min(100) int delayMs) {
}
