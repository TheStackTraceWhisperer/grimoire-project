package io.grimoire.launcher.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Represents the local cache file that stores the resolved manifest and file paths.
 */
public class LocalCache {
    
    @JsonProperty("manifest")
    private Manifest manifest;
    
    @JsonProperty("resolvedFilePaths")
    private List<String> resolvedFilePaths;
    
    public LocalCache() {
    }
    
    public LocalCache(Manifest manifest, List<String> resolvedFilePaths) {
        this.manifest = manifest;
        this.resolvedFilePaths = resolvedFilePaths;
    }
    
    public Manifest getManifest() {
        return manifest;
    }
    
    public void setManifest(Manifest manifest) {
        this.manifest = manifest;
    }
    
    public List<String> getResolvedFilePaths() {
        return resolvedFilePaths;
    }
    
    public void setResolvedFilePaths(List<String> resolvedFilePaths) {
        this.resolvedFilePaths = resolvedFilePaths;
    }
}
