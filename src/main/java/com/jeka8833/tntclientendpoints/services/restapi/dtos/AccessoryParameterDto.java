package com.jeka8833.tntclientendpoints.services.restapi.dtos;

import jakarta.validation.constraints.NotEmpty;

import java.util.Optional;

public record AccessoryParameterDto(@NotEmpty String modelConfig, Optional<String> modelTexture) {
}
