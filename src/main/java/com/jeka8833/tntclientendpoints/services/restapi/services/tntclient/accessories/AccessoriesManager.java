package com.jeka8833.tntclientendpoints.services.restapi.services.tntclient.accessories;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeka8833.tntclientendpoints.services.restapi.dtos.AccessoryParameterDto;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Service
public class AccessoriesManager {
    private static final TypeReference<HashMap<String, AccessoryParameterDto>> typeRef = new TypeReference<>() {
    };

    private final URL accessoriesListUrl;
    private final Executor executor;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final long minUpdateIntervalNanos;

    private final Lock lock = new ReentrantLock();

    private Map<String, AccessoryParameterDto> accessories;
    private long lastUpdate;

    public AccessoriesManager(@Value("${tntclient.accessories.list.url}") URL accessoriesListUrl,
                              @Value("${tntclient.accessories.list.update.time:15m}") Duration minUpdateInterval,
                              @Qualifier("virtual") Executor executor, OkHttpClient httpClient,
                              ObjectMapper objectMapper) {
        this.accessoriesListUrl = accessoriesListUrl;
        this.executor = executor;
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.minUpdateIntervalNanos = minUpdateInterval.toNanos();
        this.lastUpdate = System.nanoTime() - this.minUpdateIntervalNanos;
    }

    @Nullable
    public AccessoryParameterDto getAccessory(@NotNull String name) throws RuntimeException {
        lock.lock();

        try {
            if (accessories == null ||
                    (System.nanoTime() - lastUpdate > minUpdateIntervalNanos && !accessories.containsKey(name))) {
                lastUpdate = System.nanoTime();

                try {
                    accessories = forceReadAccessories();
                } catch (Exception e) {
                    throw new RuntimeException("Fail to read accessories list");
                }
            }

            updateAsync();
        } finally {
            lock.unlock();
        }

        return accessories.get(name);
    }

    private void updateAsync() {
        if (System.nanoTime() - lastUpdate > minUpdateIntervalNanos * 10) {
            lastUpdate = System.nanoTime();

            executor.execute(() -> {
                lock.lock();
                try {
                    accessories = forceReadAccessories();
                } catch (Exception e) {
                    log.info("Fail to async read accessories", e);
                } finally {
                    lock.unlock();
                }
            });
        }
    }

    @NotNull
    private Map<String, AccessoryParameterDto> forceReadAccessories() throws IOException {
        Request request = new Request.Builder().url(accessoriesListUrl).build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new RuntimeException("Response code: " + response.code());

            try (ResponseBody responseBody = response.body();
                 InputStream inputStream = responseBody.byteStream()) {
                Map<String, AccessoryParameterDto> value = objectMapper.readValue(inputStream, typeRef);
                if (value == null) throw new RuntimeException("Fail to read accessories");

                return value;
            }
        }
    }
}
