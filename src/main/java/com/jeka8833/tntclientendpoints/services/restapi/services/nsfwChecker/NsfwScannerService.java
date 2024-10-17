package com.jeka8833.tntclientendpoints.services.restapi.services.nsfwChecker;

@FunctionalInterface
public interface NsfwScannerService {
    NsfwResult scan(byte[] imageFileInBytes);
}
