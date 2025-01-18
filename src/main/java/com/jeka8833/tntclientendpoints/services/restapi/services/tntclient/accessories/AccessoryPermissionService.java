package com.jeka8833.tntclientendpoints.services.restapi.services.tntclient.accessories;

import com.jeka8833.tntclientendpoints.services.general.tntclintapi.database.UserRole;
import com.jeka8833.tntclientendpoints.services.restapi.dtos.web.PostAccessoriesDto;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AccessoryPermissionService {
    private final SeasonalAccessoriesManager seasonalAccessoriesManager;

    public boolean hasPermission(@NotNull PostAccessoriesDto accessoriesDto,
                                 @NotNull Authentication authentication) {
        if (accessoriesDto.accessories().length == 0) return true;  // Bypass privilege validation

        if (authentication.isAuthenticated() && authentication.getAuthorities().contains(UserRole.HAS_ACCESSORIES)) {
            return true;
        }

        try {
            Set<String> seasonalAccessories = seasonalAccessoriesManager.getActiveSeasonalAccessory();

            for (String accessory : accessoriesDto.accessories()) {
                if (!seasonalAccessories.contains(accessory)) return false;
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
