package com.jeka8833.tntclientendpoints.services.restapi.controllers.web;

import com.jeka8833.tntclientendpoints.services.restapi.dtos.web.PostAccessoriesDto;
import com.jeka8833.tntclientendpoints.services.restapi.dtos.web.PostCapeDto;
import com.jeka8833.tntclientendpoints.services.restapi.dtos.web.PostTabDto;
import com.jeka8833.tntclientendpoints.services.restapi.services.tntclient.ProfileService;
import com.jeka8833.tntclientendpoints.services.restapi.services.tntclient.accessories.AccessoriesService;
import com.jeka8833.tntclientendpoints.services.restapi.services.tntclient.cape.CapeService;
import com.jeka8833.tntclientendpoints.services.restapi.services.tntclient.tab.TabService;
import com.jeka8833.tntclientendpoints.services.restapi.services.web.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@AllArgsConstructor
final class PlayerProfileController {
    private final UserService userService;
    private final CapeService capeService;
    private final TabService tabService;
    private final ProfileService profileService;
    private final AccessoriesService accessoriesService;

    @PutMapping("api/v1/player/profile/cape")
    private void updateCape(@RequestBody PostCapeDto capeDto, Authentication authentication) {
        // Manual validation because broken optional support
        if (capeDto.data().isPresent() && capeDto.data().get().isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Cape data cannot be empty");
        }

        UUID player = userService.getUserOrThrow(authentication);

        capeService.updateCape(player, capeDto);
    }

    @DeleteMapping("api/v1/player/profile/cape")
    private void removeCape(Authentication authentication) {
        UUID player = userService.getUserOrThrow(authentication);

        capeService.removeCape(player);
    }

    @PutMapping("api/v1/player/profile/tab")
    private void updateTab(@RequestBody @Valid PostTabDto tabDto, Authentication authentication) {
        UUID player = userService.getUserOrThrow(authentication);

        tabService.updateTab(player, tabDto);
    }

    @DeleteMapping("api/v1/player/profile/tab")
    private void removeTab(Authentication authentication) {
        UUID player = userService.getUserOrThrow(authentication);

        tabService.removeTab(player);
    }

    @PutMapping("api/v1/player/profile/accessories")
    private void updateAccessories(@RequestBody @Valid PostAccessoriesDto accessoriesDto,
                                   Authentication authentication) {
        UUID player = userService.getUserOrThrow(authentication);

        accessoriesService.updateAccessories(player, accessoriesDto);
    }

    @DeleteMapping("api/v1/player/profile/accessories")
    private void removeAccessories(Authentication authentication) {
        UUID player = userService.getUserOrThrow(authentication);

        accessoriesService.removeAccessories(player);
    }

    @DeleteMapping("api/v1/player/profile")
    private void removeProfile(Authentication authentication) {
        UUID player = userService.getUserOrThrow(authentication);

        profileService.deleteProfile(player);
    }
}
