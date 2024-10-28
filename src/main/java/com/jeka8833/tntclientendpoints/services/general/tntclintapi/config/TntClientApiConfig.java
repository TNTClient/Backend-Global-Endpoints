package com.jeka8833.tntclientendpoints.services.general.tntclintapi.config;

import com.jeka8833.tntclientendpoints.services.general.tntclintapi.TNTClientApi;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.Executors;

@Configuration
public class TntClientApiConfig {

    @Bean
    public TNTClientApi tntClientAPI(OkHttpClient okHttpClient,
                                     @Value("${tntclient.api.url:wss://tnthypixel.jeka8833.pp.ua}") String requestUrl,
                                     @Value("${tntclient.api.user}") UUID user,
                                     @Value("${tntclient.api.password}") UUID password,
                                     @Value("${tntclient.api.reconnectdelay:10s}") Duration reconnectDelay) {
        return new TNTClientApi(
                new Request.Builder().url(requestUrl).build(),
                okHttpClient,
                user,
                password,
                reconnectDelay.toNanos(),
                Executors.newSingleThreadScheduledExecutor()
        );
    }
}
