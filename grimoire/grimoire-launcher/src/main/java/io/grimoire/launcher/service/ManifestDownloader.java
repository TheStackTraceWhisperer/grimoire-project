package io.grimoire.launcher.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.grimoire.launcher.model.Manifest;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Service for downloading the game manifest from the CDN.
 */
@Singleton
@Slf4j
public class ManifestDownloader {
    
    // This should be configured via property file or environment variable in production
    private static final String DEFAULT_MANIFEST_URL = "https://storage.googleapis.com/grimoire-cdn/version/grimoire-manifest.json";
    
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
    private final ObjectMapper objectMapper;
    
    public ManifestDownloader(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    /**
     * Downloads and parses the manifest from the CDN.
     */
    public Manifest downloadManifest() throws IOException, InterruptedException {
        return downloadManifest(DEFAULT_MANIFEST_URL);
    }
    
    /**
     * Downloads and parses the manifest from a specific URL.
     */
    public Manifest downloadManifest(String manifestUrl) throws IOException, InterruptedException {
        log.info("Downloading manifest from: {}", manifestUrl);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(manifestUrl))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            throw new IOException("Failed to download manifest: HTTP " + response.statusCode());
        }
        
        Manifest manifest = objectMapper.readValue(response.body(), Manifest.class);
        log.info("Downloaded manifest for version: {}", manifest.getVersion());
        
        return manifest;
    }
}
