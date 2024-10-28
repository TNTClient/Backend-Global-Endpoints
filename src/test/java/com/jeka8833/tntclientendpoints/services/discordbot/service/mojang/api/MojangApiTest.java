package com.jeka8833.tntclientendpoints.services.discordbot.service.mojang.api;

import com.jeka8833.tntclientendpoints.services.discordbot.service.mojang.MojangProfile;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class MojangApiTest {
    @Autowired
    private MojangApi mojangApi;

    @Test
    void getProfiles() {
        UUID[] uuids = new UUID[]{
                UUID.fromString("6bd6e833-a80a-430e-9029-4786368811f9"),
                UUID.fromString("65d53130-953b-4e01-811a-8135d5884ba8"),
                UUID.fromString("653c85d2-d8cf-4605-83ad-544da546d579"),
                UUID.fromString("ee67256e-5dd2-475e-a8a9-3b9ad0ffaf4b"),
                UUID.fromString("60850918-d486-4b49-afe3-f6b7e70cbc97")
        };
        System.out.println(Thread.currentThread().getName());
        Map<UUID, MojangProfile> profiles = mojangApi.getProfiles(Set.of(uuids));
        System.out.println(Thread.currentThread().getName());

    }

    @Test
    void getProfileAsync() {
        UUID uuid = UUID.fromString("6bd6e833-a80a-430e-9029-4786368811f9");
        for (int i = 0; i < 10; i++) {
            long time = System.currentTimeMillis();
            CompletableFuture<MojangProfile> profileCompl = mojangApi.getProfileAsync(uuid);
            System.out.println(System.currentTimeMillis());
            MojangProfile profile = profileCompl.join();
            long time2 = System.currentTimeMillis();
            System.out.println("Time: " + (time2 - time) + " " + System.currentTimeMillis());

            assertEquals(uuid, profile.uuid());
            assertEquals("Jeka8833", profile.name());
        }
    }
}