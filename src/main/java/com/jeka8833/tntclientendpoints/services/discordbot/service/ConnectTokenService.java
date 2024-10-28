package com.jeka8833.tntclientendpoints.services.discordbot.service;

import com.jeka8833.tntclientendpoints.services.discordbot.models.ConnectedPlayerModel;
import com.jeka8833.tntclientendpoints.services.discordbot.repositories.ConnectedPlayerRepository;
import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class ConnectTokenService {
    private static final Random RANDOM = new SecureRandom();

    private final Cache<Integer, Long> cache;
    private final ConnectedPlayerRepository connectedPlayerRepository;

    public ConnectTokenService(ConnectedPlayerRepository connectedPlayerRepository) {
        this.connectedPlayerRepository = connectedPlayerRepository;

        cache = new Cache2kBuilder<Integer, Long>() {
        }.expireAfterWrite(1, TimeUnit.MINUTES)
                .sharpExpiry(true)
                .build();
    }

    public int generateToken(long discordID) {
        int token;
        do {
            token = RANDOM.nextInt(100_000, 1_000_000);
        } while (!cache.putIfAbsent(token, discordID));

        return token;
    }

    public boolean connect(UUID player, int token) {
        Long discordID = cache.peekAndRemove(token);
        if (discordID == null) return false;

        connectedPlayerRepository.save(new ConnectedPlayerModel(discordID, player));

        return true;
    }

    public void disconnect(long discordID) {
        connectedPlayerRepository.deleteById(discordID);
    }
}
