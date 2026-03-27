package io.grimoire.launcher.service;

import io.grimoire.launcher.model.LocalCache;
import io.grimoire.launcher.model.Manifest;
import io.grimoire.launcher.model.Repository;
import io.grimoire.launcher.util.LauncherPaths;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.Authentication;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.resolution.DependencyResult;
import org.eclipse.aether.supplier.RepositorySystemSupplier;
import org.eclipse.aether.util.repository.AuthenticationBuilder;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for resolving and downloading Maven artifacts using Maven Resolver.
 */
@Singleton
@Slf4j
public class ArtifactResolver {
    
    // GitHub Personal Access Token (obfuscated in production)
    // This should be read from an encrypted configuration or obfuscated in the code
    private static final String GITHUB_PAT = System.getenv("GRIMOIRE_GITHUB_TOKEN");
    
    /**
     * Resolves all artifacts for the given manifest and returns a LocalCache.
     */
    public LocalCache resolveArtifacts(Manifest manifest) throws DependencyResolutionException, IOException {
        log.info("Resolving artifacts for version: {}", manifest.getVersion());
        
        // Create repository system
        RepositorySystem system = new RepositorySystemSupplier().get();
        
        // Create session with local cache directory
        Path cacheDir = LauncherPaths.getCacheDirectory();
        DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();
        LocalRepository localRepo = new LocalRepository(cacheDir.toFile());
        session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepo));
        
        // Build remote repositories from manifest
        List<RemoteRepository> repositories = buildRemoteRepositories(manifest.getRepositories());
        
        // Parse main artifact coordinate
        Artifact mainArtifact = new DefaultArtifact(manifest.getMainArtifact());
        
        // Create collect request
        CollectRequest collectRequest = new CollectRequest();
        collectRequest.setRoot(new Dependency(mainArtifact, "compile"));
        collectRequest.setRepositories(repositories);
        
        // Resolve dependencies
        DependencyRequest dependencyRequest = new DependencyRequest(collectRequest, null);
        DependencyResult result = system.resolveDependencies(session, dependencyRequest);
        
        // Extract file paths
        List<String> filePaths = result.getArtifactResults().stream()
                .map(ArtifactResult::getArtifact)
                .map(Artifact::getFile)
                .map(file -> file.getAbsolutePath())
                .collect(Collectors.toList());
        
        log.info("Resolved {} artifacts", filePaths.size());
        
        return new LocalCache(manifest, filePaths);
    }
    
    /**
     * Builds RemoteRepository objects from manifest repositories.
     */
    private List<RemoteRepository> buildRemoteRepositories(List<Repository> repositories) {
        List<RemoteRepository> remoteRepos = new ArrayList<>();
        
        for (Repository repo : repositories) {
            RemoteRepository.Builder builder = new RemoteRepository.Builder(repo.getId(), "default", repo.getUrl());
            
            // Add authentication for GitHub Packages
            if (repo.getUrl().contains("maven.pkg.github.com") && GITHUB_PAT != null) {
                Authentication auth = new AuthenticationBuilder()
                        .addUsername("token")
                        .addPassword(GITHUB_PAT)
                        .build();
                builder.setAuthentication(auth);
                log.info("Configured authentication for repository: {}", repo.getId());
            }
            
            remoteRepos.add(builder.build());
        }
        
        return remoteRepos;
    }
}
