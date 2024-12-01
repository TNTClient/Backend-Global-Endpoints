package com.jeka8833.tntclientendpoints.services.restapi.controllers.web;

import com.jeka8833.tntclientendpoints.services.restapi.models.AccessoryReloadResponse;
import com.jeka8833.tntclientendpoints.services.restapi.services.tntclient.accessories.AccessoriesManager;
import com.jeka8833.tntclientendpoints.services.restapi.services.tntclient.accessories.SeasonalAccessoriesManager;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AdminController {

    private final AccessoriesManager accessoriesManager;
    private final SeasonalAccessoriesManager seasonalAccessoriesManager;

    @PostMapping("api/v1/admin/reset/accessory/cache")
    private AccessoryReloadResponse resetAccessoryCache() {
        return new AccessoryReloadResponse(
                accessoriesManager.tryReloadAccessory(),
                seasonalAccessoriesManager.tryReloadSeasonalList()
        );
    }
}
