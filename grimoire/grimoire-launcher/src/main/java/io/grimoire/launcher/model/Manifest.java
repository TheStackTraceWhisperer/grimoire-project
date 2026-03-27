package io.grimoire.launcher.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Represents the grimoire-manifest.json file that defines the game version and configuration.
 */
public class Manifest {
    
    @JsonProperty("displayName")
    private String displayName;
    
    @JsonProperty("version")
    private String version;
    
    @JsonProperty("mainArtifact")
    private String mainArtifact;
    
    @JsonProperty("mainClass")
    private String mainClass;
    
    @JsonProperty("jvmArgs")
    private List<String> jvmArgs;
    
    @JsonProperty("repositories")
    private List<Repository> repositories;
    
    public Manifest() {
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public String getMainArtifact() {
        return mainArtifact;
    }
    
    public void setMainArtifact(String mainArtifact) {
        this.mainArtifact = mainArtifact;
    }
    
    public String getMainClass() {
        return mainClass;
    }
    
    public void setMainClass(String mainClass) {
        this.mainClass = mainClass;
    }
    
    public List<String> getJvmArgs() {
        return jvmArgs;
    }
    
    public void setJvmArgs(List<String> jvmArgs) {
        this.jvmArgs = jvmArgs;
    }
    
    public List<Repository> getRepositories() {
        return repositories;
    }
    
    public void setRepositories(List<Repository> repositories) {
        this.repositories = repositories;
    }
}
