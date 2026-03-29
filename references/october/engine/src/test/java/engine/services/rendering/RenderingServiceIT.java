package engine.services.rendering;

import engine.EngineTestHarness;
import engine.services.resources.AssetCacheService;
import engine.services.window.WindowService;
import jakarta.inject.Inject;
import org.joml.Matrix4f;
import org.junit.jupiter.api.Test;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.lwjgl.opengl.GL11.*;

public class RenderingServiceIT extends EngineTestHarness {

    @Inject
    private RenderingService renderingService;
    @Inject
    private AssetCacheService assetCacheService;
    @Inject
    private WindowService windowService;
    @Inject
    private CameraService cameraService;

    @Test
    void testServiceDirectSubmission() {
        // Given: A red procedural texture
        ByteBuffer redPixel = createPixel((byte) 255, (byte) 0, (byte) 0);
        Texture redTexture = new Texture(1, 1, redPixel);

        // And: A standard quad mesh and a transform to place it at the center of the screen
        Mesh quadMesh = assetCacheService.resolveMeshHandle("quad");
        Matrix4f transform = new Matrix4f().identity().scale(5.0f); // A large quad

        // When: We directly call the rendering service to submit and render the sprite
        renderingService.beginScene(cameraService);
        renderingService.submit(quadMesh, redTexture, transform);
        renderingService.endScene(); // This is where the instanced drawing happens

        // Then: The center pixel of the back buffer should be red
        PixelColor centerColor = readPixelColorFromBackBuffer(windowService.getWidth() / 2, windowService.getHeight() / 2);

        // Clean up
        redTexture.close();
        MemoryUtil.memFree(redPixel);

        assertEquals(255, centerColor.r, "Center pixel red channel");
        assertEquals(0, centerColor.g, "Center pixel green channel");
        assertEquals(0, centerColor.b, "Center pixel blue channel");
    }

    private ByteBuffer createPixel(byte r, byte g, byte b) {
        ByteBuffer pixel = MemoryUtil.memAlloc(4);
        return pixel.put(r).put(g).put(b).put((byte) 255).flip();
    }

    private PixelColor readPixelColorFromBackBuffer(int x, int y) {
        glReadBuffer(GL_BACK);
        ByteBuffer buffer = ByteBuffer.allocateDirect(4);
        glReadPixels(x, y, 1, 1, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
        int r = buffer.get(0) & 0xFF;
        int g = buffer.get(1) & 0xFF;
        int b = buffer.get(2) & 0xFF;
        return new PixelColor(r, g, b);
    }

    private record PixelColor(int r, int g, int b) {}
}
