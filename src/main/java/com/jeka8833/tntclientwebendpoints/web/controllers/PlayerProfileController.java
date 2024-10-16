package com.jeka8833.tntclientwebendpoints.web.controllers;

import com.jeka8833.tntclientwebendpoints.web.dtos.PostCapeDto;
import com.jeka8833.tntclientwebendpoints.web.dtos.PostTabDto;
import com.jeka8833.tntclientwebendpoints.web.services.tntclient.ProfileService;
import com.jeka8833.tntclientwebendpoints.web.services.tntclient.cape.CapeService;
import com.jeka8833.tntclientwebendpoints.web.services.tntclient.tab.TabService;
import com.jeka8833.tntclientwebendpoints.web.services.web.UserService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@AllArgsConstructor
final class PlayerProfileController {
    private final UserService userService;
    private final CapeService capeService;
    private final TabService tabService;
    private final ProfileService profileService;

    @PutMapping("api/v1/player/profile/cape")
    private void updateCape(@RequestBody PostCapeDto capeDto, Authentication authentication) {
        UUID player = userService.getUserOrThrow(authentication);

        capeService.updateCape(player, capeDto);
    }

    @DeleteMapping("api/v1/player/profile/cape")
    private void removeCape(Authentication authentication) {
        UUID player = userService.getUserOrThrow(authentication);

        capeService.removeCape(player);
    }

    @PutMapping("api/v1/player/profile/tab")
    private void updateTab(@RequestBody PostTabDto tabDto, Authentication authentication) {
        UUID player = userService.getUserOrThrow(authentication);

        tabService.updateTab(player, tabDto);
    }

    @DeleteMapping("api/v1/player/profile/tab")
    private void removeTab(Authentication authentication) {
        UUID player = userService.getUserOrThrow(authentication);

        tabService.removeTab(player);
    }

    @DeleteMapping("api/v1/player/profile")
    private void removeProfile(Authentication authentication) {
        UUID player = userService.getUserOrThrow(authentication);

        profileService.deleteProfile(player);
    }
}
