# Grimoire Launcher

A standalone JavaFX launcher application that manages game client updates and launches the Grimoire game client.

## Overview

The Grimoire Launcher is a self-contained application that:
- Downloads and parses the game manifest from a CDN
- Resolves and downloads game client artifacts and dependencies using Maven
- Compares versions and only downloads updates when needed
- Launches the game client with proper JVM arguments
- Maintains a local cache of downloaded artifacts

## Architecture

```
[CDN (S3/GCS)]
    └─ /version/grimoire-manifest.json
    └─ /launcher/grimoire-launcher.jar

[GitHub Packages]
    └─ com.grimoire:grimoire-client
    └─ com.grimoire:grimoire-shared

[User's PC]
    └─ AppData/Grimoire/
        ├─ cache/ (Maven local repository)
        └─ local-cache.json
```

## Building the Launcher

```bash
cd grimoire-launcher
mvn clean package
```

This creates `target/grimoire-launcher-1.0.1-shaded.jar` which is the distributable launcher.

## Running the Launcher

```bash
java -jar grimoire-launcher-1.0.1-shaded.jar
```

### Environment Variables

- `GRIMOIRE_GITHUB_TOKEN`: Personal Access Token with `read:packages` scope for accessing GitHub Packages

## Configuration

The launcher reads the manifest from:
```
https://storage.googleapis.com/grimoire-cdn/version/grimoire-manifest.json
```

This URL can be modified in `ManifestDownloader.java`.

## Manifest Format

```json
{
  "displayName": "Grimoire",
  "version": "1.0.1",
  "mainArtifact": "com.grimoire:grimoire-client:1.0.1",
  "mainClass": "com.grimoire.client.Main",
  "jvmArgs": [
    "-Xmx2g",
    "--module-path", "%FX_MODULE_PATH%",
    "--add-modules", "javafx.controls,javafx.fxml"
  ],
  "repositories": [
    {
      "id": "grimoire-repo",
      "url": "https://maven.pkg.github.com/YOUR_ORG/grimoire"
    },
    {
      "id": "maven-central",
      "url": "https://repo1.maven.org/maven2/"
    }
  ]
}
```

## Local Cache

After resolving artifacts, the launcher saves a `local-cache.json` file:

```json
{
  "manifest": { ... },
  "resolvedFilePaths": [
    "C:/Users/User/AppData/Grimoire/cache/com/grimoire/grimoire-client/1.0.1/grimoire-client-1.0.1.jar",
    ...
  ]
}
```

This allows the launcher to skip downloads if the version hasn't changed.

## Deployment Workflow

1. Developer runs `mvn deploy -P release` to publish artifacts to GitHub Packages
2. CI/CD workflow updates the `grimoire-manifest.json` on the CDN
3. User runs the launcher
4. Launcher checks manifest, downloads updates if needed, launches game

## Cloud Infrastructure Setup

### GitHub Packages

1. Create a Personal Access Token with `read:packages` and `write:packages` scopes
2. Configure GitHub Actions secrets:
   - `GITHUB_TOKEN` is automatically provided by GitHub Actions

### CDN Setup (S3/GCS)

1. Create a bucket with public read access
2. Enable CORS:
   ```json
   [
     {
       "AllowedOrigins": ["*"],
       "AllowedMethods": ["GET"],
       "AllowedHeaders": ["*"],
       "MaxAgeSeconds": 3000
     }
   ]
   ```
3. Upload files:
   - `/launcher/grimoire-launcher.jar` - The launcher itself
   - `/version/grimoire-manifest.json` - The version manifest

## Requirements

- Java 21 or higher
- Network access to GitHub Packages and Maven Central
- GitHub Personal Access Token (for private repositories)

## Security Considerations

- The GitHub PAT is bundled in the launcher (should be obfuscated)
- Code signing with jarsigner is recommended for production
- Manifest should be served over HTTPS only
- Consider implementing checksum verification for artifacts

## License

This project is part of the Grimoire MMO-lite game.
