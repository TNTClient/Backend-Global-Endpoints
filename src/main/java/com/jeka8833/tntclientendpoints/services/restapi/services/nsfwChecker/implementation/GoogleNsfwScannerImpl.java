package com.jeka8833.tntclientendpoints.services.restapi.services.nsfwChecker.implementation;

import com.google.cloud.spring.vision.CloudVisionTemplate;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Likelihood;
import com.google.cloud.vision.v1.SafeSearchAnnotation;
import com.jeka8833.tntclientendpoints.services.restapi.services.nsfwChecker.NsfwResult;
import com.jeka8833.tntclientendpoints.services.restapi.services.nsfwChecker.NsfwScannerService;
import io.github.bucket4j.Bucket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
public class GoogleNsfwScannerImpl implements NsfwScannerService {
    private final CloudVisionTemplate cloudVisionService;
    private final Bucket bucket;

    @Autowired
    public GoogleNsfwScannerImpl(CloudVisionTemplate cloudVisionService,
                                 @Value(value = "${spring.cloud.gcp.vision.limitperday}") int limitPerDay) {
        this.cloudVisionService = cloudVisionService;
        this.bucket = Bucket.builder()
                .addLimit(limit -> limit.capacity(limitPerDay).refillIntervally(limitPerDay, Duration.ofDays(1L)))
                .build();
    }

    @Override
    public NsfwResult scan(byte[] imageFileInBytes) {
        try {
            if (!bucket.tryConsume(1L)) {
                log.warn("NSFW scan limit reached");

                return NsfwResult.UNKNOWN;
            }

            var byteArrayResource = new ByteArrayResource(imageFileInBytes);
            AnnotateImageResponse response =
                    cloudVisionService.analyzeImage(byteArrayResource, Feature.Type.SAFE_SEARCH_DETECTION);
            if (response.hasError()) {
                log.warn("NSFW scanner has error: {}", response.getError().getMessage());

                return NsfwResult.UNKNOWN;
            }

            SafeSearchAnnotation safeSearchAnnotation = response.getSafeSearchAnnotation();
            log.info("NSFW scan result:\nAdult: {}\nSpoof: {}\nMedical: {}\nViolence: {}\nRacy: {}",
                    safeSearchAnnotation.getAdult(), safeSearchAnnotation.getSpoof(), safeSearchAnnotation.getMedical(),
                    safeSearchAnnotation.getViolence(), safeSearchAnnotation.getRacy());

            if (safeSearchAnnotation.getAdultValue() >= Likelihood.POSSIBLE_VALUE) return NsfwResult.UNSAFE;
            if (safeSearchAnnotation.getViolenceValue() >= Likelihood.LIKELY_VALUE) return NsfwResult.UNSAFE;
            if (safeSearchAnnotation.getRacyValue() >= Likelihood.VERY_LIKELY_VALUE) return NsfwResult.UNSAFE;

            return NsfwResult.SAFE;
        } catch (Exception e) {
            log.warn("NSFW scanner has error: {}", e.getMessage());

            return NsfwResult.UNKNOWN;
        }
    }
}