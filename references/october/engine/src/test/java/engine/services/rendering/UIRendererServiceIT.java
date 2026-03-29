package engine.services.rendering;

import engine.EngineTestHarness;
import engine.services.resources.AssetCacheService;
import engine.services.window.WindowService;
import engine.services.world.components.UITransformComponent;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.lwjgl.opengl.GL11.*;

public class UIRendererServiceIT extends EngineTestHarness {

    @Inject
    private UIRendererService uiRendererService;
    @Inject
    private AssetCacheService assetCacheService;
    @Inject
    private WindowService windowService;

    @Test
    void testServiceDirectSubmission() {
        // Given: A procedural blue texture
        ByteBuffer bluePixel = MemoryUtil.memAlloc(4).put((byte) 0).put((byte) 0).put((byte) 255).put((byte) 255).flip();
        assetCacheService.addTexture("blue-texture", new Texture(1, 1, bluePixel));

        // And: A UI transform with manually calculated screen bounds, since the UISystem is not running.
        var transform = new UITransformComponent();
        int screenWidth = windowService.getWidth();
        int screenHeight = windowService.getHeight();
        float imageWidth = 100;
        float imageHeight = 100;
        transform.screenBounds[0] = (screenWidth / 2f) - (imageWidth / 2f); // minX
        transform.screenBounds[1] = (screenHeight / 2f) - (imageHeight / 2f); // minY
        transform.screenBounds[2] = (screenWidth / 2f) + (imageWidth / 2f); // maxX
        transform.screenBounds[3] = (screenHeight / 2f) + (imageHeight / 2f); // maxY

        // And: A clean framebuffer
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT);

        // When: We directly call the UI rendering service
        uiRendererService.begin();
        uiRendererService.submit(transform, "blue-texture");
        uiRendererService.end();

        // Then: The center pixel of the back buffer should be blue
        var pixelColor = readPixelColorFromBackBuffer(screenWidth / 2, screenHeight / 2);

        // Clean up
        MemoryUtil.memFree(bluePixel);

        assertEquals(0, pixelColor.r, "Red channel should be 0");
        assertEquals(0, pixelColor.g, "Green channel should be 0");
        assertEquals(255, pixelColor.b, "Blue channel should be 255");
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
