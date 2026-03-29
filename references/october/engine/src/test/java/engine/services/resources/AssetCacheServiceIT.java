package engine.services.resources;

import engine.EngineTestHarness;
import engine.services.audio.AudioBuffer;
import engine.services.rendering.Mesh;
import engine.services.rendering.Texture;
import engine.services.rendering.gl.Shader;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

public class AssetCacheServiceIT extends EngineTestHarness {

    @Inject
    private AssetCacheService assetCacheService;

    @Test
    void testAssetLoadingAndCaching() {
        // Given: Valid paths to test assets
        String texturePath = "textures/valid_texture.png";
        String vertexShaderPath = "/shaders/default.vert";
        String fragmentShaderPath = "/shaders/default.frag";
        String audioPath = "audio/test-sound.ogg";

        // When: We load each asset type for the first time
        Texture texture1 = assetCacheService.loadTexture("test-texture", texturePath);
        Shader shader1 = assetCacheService.loadShader("test-shader", vertexShaderPath, fragmentShaderPath);
        AudioBuffer audioBuffer1 = assetCacheService.loadAudioBuffer("test-audio", audioPath);
        Mesh quadMesh = assetCacheService.resolveMeshHandle("quad"); // Default mesh

        // Then: The assets should be successfully loaded
        assertNotNull(texture1, "Texture should be loaded.");
        assertNotNull(shader1, "Shader should be loaded.");
        assertNotNull(audioBuffer1, "AudioBuffer should be loaded.");
        assertNotNull(quadMesh, "Default quad mesh should be resolvable.");

        // When: We load the same assets again with the same handles
        Texture texture2 = assetCacheService.loadTexture("test-texture", texturePath);
        Shader shader2 = assetCacheService.loadShader("test-shader", vertexShaderPath, fragmentShaderPath);
        AudioBuffer audioBuffer2 = assetCacheService.loadAudioBuffer("test-audio", audioPath);

        // Then: The service should return the identical, cached object instance
        assertSame(texture1, texture2, "Loading the same texture handle should return the cached instance.");
        assertSame(shader1, shader2, "Loading the same shader handle should return the cached instance.");
        assertSame(audioBuffer1, audioBuffer2, "Loading the same audio handle should return the cached instance.");
    }
}
