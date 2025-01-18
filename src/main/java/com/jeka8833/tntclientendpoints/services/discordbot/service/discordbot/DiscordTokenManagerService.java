package com.jeka8833.tntclientendpoints.services.discordbot.service.discordbot;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.jeka8833.tntclientendpoints.services.discordbot.models.ConnectedPlayerModel;
import com.jeka8833.tntclientendpoints.services.discordbot.repositories.ConnectedPlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class DiscordTokenManagerService {
    private static final Random RANDOM = new SecureRandom();

    private final ConnectedPlayerRepository connectedPlayerRepository;

    private final Cache<Integer, Long> cache = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .build();

    public int generateToken(long discordID) {
        int token;
        do {
            token = RANDOM.nextInt(100_000, 1_000_000);
        } while (cache.asMap().putIfAbsent(token, discordID) != null);

        return token;
    }

    public boolean validateAndConnect(UUID player, int token) {
        Long discordID = cache.asMap().remove(token);
        if (discordID == null) return false;

        connectedPlayerRepository.save(new ConnectedPlayerModel(discordID, player));

        return true;
    }

    public void disconnect(long discordID) {
        connectedPlayerRepository.deleteById(discordID);
    }
}
