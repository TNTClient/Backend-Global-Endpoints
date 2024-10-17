package com.jeka8833.tntclientendpoints.services.restapi;

import com.jeka8833.tntclientendpoints.services.mojang.MojangServiceRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RunRestApi {
    public static void main(String[] args) {
        SpringApplication.run(new Class[]{RunRestApi.class, MojangServiceRunner.class}, args);
    }
}
