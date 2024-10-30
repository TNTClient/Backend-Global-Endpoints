package com.jeka8833.tntclientendpoints.services.restapi.dtos.web;

import jakarta.validation.constraints.NotBlank;

import java.util.Optional;

public record PostCapeDto(boolean enabled, Optional<@NotBlank String> data) {
}
