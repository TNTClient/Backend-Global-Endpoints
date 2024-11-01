package com.jeka8833.tntclientendpoints.services.restapi.services.nsfwChecker.implementation;

import com.jeka8833.tntclientendpoints.services.restapi.services.nsfwChecker.NsfwResult;
import com.jeka8833.tntclientendpoints.services.restapi.services.nsfwChecker.NsfwScannerService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(value = "tntclient.nsfw.scanner", havingValue = "alwaysgood")
public class AlwaysSafeScannerImpl implements NsfwScannerService {
    @Override
    public NsfwResult scan(byte[] imageFileInBytes) {
        return NsfwResult.SAFE;
    }
}
