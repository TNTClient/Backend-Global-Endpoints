package com.jeka8833.tntclientendpoints.services.restapi.services.tntclient.accessories;

import com.jeka8833.tntclientendpoints.services.restapi.dtos.web.PostAccessoriesDto;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AccessoryPermissionService {
    private static final SimpleGrantedAuthority ACCESSORY_AUTHORITY = new SimpleGrantedAuthority("ACCESSORIES");

    private final SeasonalAccessoriesManager seasonalAccessoriesManager;

    public boolean hasPermission(@NotNull PostAccessoriesDto accessoriesDto, @NotNull Authentication authentication) {
        if (accessoriesDto.accessories().length == 0) return true;

        if (authentication.isAuthenticated() && authentication.getAuthorities().contains(ACCESSORY_AUTHORITY)) {
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
