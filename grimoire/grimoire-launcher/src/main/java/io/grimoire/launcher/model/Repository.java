package io.grimoire.launcher.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a Maven repository configuration in the manifest.
 */
public class Repository {
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("url")
    private String url;
    
    public Repository() {
    }
    
    public Repository(String id, String url) {
        this.id = id;
        this.url = url;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
}
