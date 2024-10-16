package com.jeka8833.tntclientwebendpoints.web.services.nsfwChecker;

@FunctionalInterface
public interface NsfwScannerService {
    NsfwResult scan(byte[] imageFileInBytes);
}
