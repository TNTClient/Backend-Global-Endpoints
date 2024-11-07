package com.jeka8833.tntclientendpoints.services.restapi.services.nsfwChecker.implementation;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Slf4j
@Component("googleCacheKeyGenerator")
public class GoogleCacheKey implements KeyGenerator {

    @NotNull
    @Override
    public Object generate(@NotNull Object target, @NotNull Method method, Object... params) {
        if (params.length != 1) throw new IllegalArgumentException("Invalid number of parameters");

        if (params[0] instanceof byte[] array) {
            ChecksumWrapper md5 = new ChecksumWrapper(array);

            log.info("Input image hash before checking NSFW: {}", md5);

            return md5;
        }

        throw new IllegalArgumentException("Invalid parameter type");
    }

    @ToString
    @EqualsAndHashCode
    public static class ChecksumWrapper {
        private final byte[] checksum;

        public ChecksumWrapper(byte[] image) {
            this.checksum = DigestUtils.md5(image);
        }
    }
}
