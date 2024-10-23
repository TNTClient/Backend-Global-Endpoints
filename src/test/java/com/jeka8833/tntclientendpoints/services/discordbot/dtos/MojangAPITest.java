package com.jeka8833.tntclientendpoints.services.discordbot.dtos;

import com.jeka8833.tntclientendpoints.services.discordbot.service.mojang.api.MojangAPI;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.UUID;

@SpringBootTest
class MojangAPITest {

    @Autowired
    private MojangAPI mojangAPI;

    @Test
    void getProfile() throws IOException {
        System.out.println(mojangAPI.getProfile(UUID.fromString("6bd6e833-a80a-430e-9029-4786368811f9")));


    }
}