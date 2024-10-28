package com.jeka8833.tntclientendpoints.services.restapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
        "com.jeka8833.tntclientendpoints.services.restapi",
        "com.jeka8833.tntclientendpoints.services.general"
})
public class RunRestApi {
    public static void main(String[] args) {
        SpringApplication.run(RunRestApi.class, args);
    }
}
