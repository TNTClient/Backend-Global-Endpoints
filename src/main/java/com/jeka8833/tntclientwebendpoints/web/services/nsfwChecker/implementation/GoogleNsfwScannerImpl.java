package com.jeka8833.tntclientwebendpoints.web.services.nsfwChecker.implementation;

import com.google.cloud.spring.vision.CloudVisionTemplate;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Likelihood;
import com.google.cloud.vision.v1.SafeSearchAnnotation;
import com.jeka8833.tntclientwebendpoints.web.services.nsfwChecker.NsfwResult;
import com.jeka8833.tntclientwebendpoints.web.services.nsfwChecker.NsfwScannerService;
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

        boolean isSafe = GoogleNsfwScannerImpl.isSafe(safeSearchAnnotation.getAdult()) &&
                GoogleNsfwScannerImpl.isSafe(safeSearchAnnotation.getMedical()) &&
                GoogleNsfwScannerImpl.isSafe(safeSearchAnnotation.getViolence()) &&
                GoogleNsfwScannerImpl.isSafe(safeSearchAnnotation.getRacy());

        return isSafe ? NsfwResult.SAFE : NsfwResult.UNSAFE;
    }

    private static boolean isSafe(Likelihood likelihood) {
        return switch (likelihood) {
            case VERY_UNLIKELY, UNLIKELY, UNKNOWN, UNRECOGNIZED -> true;
            default -> false;
        };
    }
}