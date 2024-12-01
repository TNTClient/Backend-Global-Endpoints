package com.jeka8833.tntclientendpoints.services.restapi.services.tntclient.accessories;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeka8833.tntclientendpoints.services.restapi.dtos.AccessoryParameterDto;
import lombok.Locked;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class AccessoriesManager {
    private static final TypeReference<HashMap<String, AccessoryParameterDto>> typeRef = new TypeReference<>() {
    };

    private final Request httpRequest;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    @NotNull
    private Map<String, AccessoryParameterDto> accessories;

    public AccessoriesManager(@Value("${tntclient.accessories.list.url}") URL accessoriesListUrl,
                              OkHttpClient httpClient, ObjectMapper objectMapper) throws IOException {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;

        this.httpRequest = new Request.Builder().url(accessoriesListUrl).build();

        this.accessories = forceReadAccessories();
    }

    @Nullable
    @Contract(value = "null -> null", pure = true)
    public AccessoryParameterDto getAccessory(@Nullable String name) {
        if (name == null) return null;

        return accessories.get(name);
    }

    public boolean tryReloadAccessory() {
        try {
            accessories = forceReadAccessories();

            return true;
        } catch (Exception e) {
            log.warn("Fail to reload accessories", e);

            return false;
        }
    }

    @NotNull
    private Map<String, AccessoryParameterDto> forceReadAccessories() throws IOException {
        try (Response response = httpClient.newCall(httpRequest).execute()) {
            if (!response.isSuccessful()) throw new RuntimeException("Response code: " + response.code());

            try (ResponseBody responseBody = response.body();
                 InputStream inputStream = responseBody.byteStream()) {
                Map<String, AccessoryParameterDto> value = objectMapper.readValue(inputStream, typeRef);
                if (value == null) throw new RuntimeException("Fail to parse accessories list");

                return value;
            }
        }
    }
}
