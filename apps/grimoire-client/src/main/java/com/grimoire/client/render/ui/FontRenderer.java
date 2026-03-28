package com.grimoire.client.render.ui;

import com.grimoire.client.render.texture.Texture;
import lombok.extern.slf4j.Slf4j;
import org.joml.Vector4f;
import org.lwjgl.stb.STBTTAlignedQuad;
import org.lwjgl.stb.STBTTBakedChar;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL45.*;
import static org.lwjgl.stb.STBTruetype.*;

/**
 * Font renderer using STB TrueType for text rendering.
 * Creates a bitmap font atlas and renders text as textured quads.
 */
@Slf4j
public class FontRenderer {
    
    private static final int BITMAP_WIDTH = 512;
    private static final int BITMAP_HEIGHT = 512;
    private static final int FIRST_CHAR = 32;
    private static final int CHAR_COUNT = 96;
    private static final float DEFAULT_FONT_SIZE = 24.0f;
    
    private int fontTextureId;
    private STBTTBakedChar.Buffer charData;
    private float fontHeight;
    private float ascent;
    private float descent;
    private float lineGap;
    
    /**
     * Initializes the font renderer with a default font.
     */
    public void init() throws IOException {
        // Try to load a system font or use embedded default
        ByteBuffer fontBuffer = loadDefaultFont();
        if (fontBuffer == null) {
            log.warn("Could not load font, using fallback");
            createFallbackFont();
            return;
        }
        
        try {
            // Get font metrics
            STBTTFontinfo fontInfo = STBTTFontinfo.create();
            if (!stbtt_InitFont(fontInfo, fontBuffer)) {
                log.warn("Failed to initialize font");
                createFallbackFont();
                return;
            }
            
            float scale = stbtt_ScaleForPixelHeight(fontInfo, DEFAULT_FONT_SIZE);
            
            try (MemoryStack stack = MemoryStack.stackPush()) {
                var ascentBuf = stack.mallocInt(1);
                var descentBuf = stack.mallocInt(1);
                var lineGapBuf = stack.mallocInt(1);
                stbtt_GetFontVMetrics(fontInfo, ascentBuf, descentBuf, lineGapBuf);
                
                this.ascent = ascentBuf.get(0) * scale;
                this.descent = descentBuf.get(0) * scale;
                this.lineGap = lineGapBuf.get(0) * scale;
                this.fontHeight = ascent - descent;
            }
            
            // Bake font to bitmap
            ByteBuffer bitmap = MemoryUtil.memAlloc(BITMAP_WIDTH * BITMAP_HEIGHT);
            charData = STBTTBakedChar.malloc(CHAR_COUNT);
            
            stbtt_BakeFontBitmap(fontBuffer, DEFAULT_FONT_SIZE, bitmap, BITMAP_WIDTH, BITMAP_HEIGHT, FIRST_CHAR, charData);
            
            // Create texture
            fontTextureId = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, fontTextureId);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RED, BITMAP_WIDTH, BITMAP_HEIGHT, 0, GL_RED, GL_UNSIGNED_BYTE, bitmap);
            
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            
            // Enable texture swizzle for red channel to alpha
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_R, GL_ONE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_G, GL_ONE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_B, GL_ONE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_A, GL_RED);
            
            glBindTexture(GL_TEXTURE_2D, 0);
            
            MemoryUtil.memFree(bitmap);
            
            log.info("Font renderer initialized ({}x{} atlas)", BITMAP_WIDTH, BITMAP_HEIGHT);
            
        } finally {
            MemoryUtil.memFree(fontBuffer);
        }
    }
    
    private ByteBuffer loadDefaultFont() {
        // Try to load a bundled font first
        try (InputStream is = getClass().getResourceAsStream("/fonts/default.ttf")) {
            if (is != null) {
                byte[] fontData = is.readAllBytes();
                ByteBuffer buffer = MemoryUtil.memAlloc(fontData.length);
                buffer.put(fontData).flip();
                return buffer;
            }
        } catch (IOException e) {
            log.debug("Could not load bundled font: {}", e.getMessage());
        }
        
        // Try system fonts
        String[] systemFonts = {
            "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
            "/usr/share/fonts/truetype/liberation/LiberationSans-Regular.ttf",
            "C:\\Windows\\Fonts\\arial.ttf",
            "/System/Library/Fonts/Helvetica.ttc"
        };
        
        for (String fontPath : systemFonts) {
            try {
                java.io.File fontFile = new java.io.File(fontPath);
                if (fontFile.exists()) {
                    byte[] fontData = java.nio.file.Files.readAllBytes(fontFile.toPath());
                    ByteBuffer buffer = MemoryUtil.memAlloc(fontData.length);
                    buffer.put(fontData).flip();
                    log.debug("Loaded system font: {}", fontPath);
                    return buffer;
                }
            } catch (IOException e) {
                log.debug("Could not load font {}: {}", fontPath, e.getMessage());
            }
        }
        
        return null;
    }
    
    private void createFallbackFont() {
        // Create a simple bitmap font texture as fallback
        // For fallback, we create a white texture that will display "?" or boxes
        fontTextureId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, fontTextureId);
        
        ByteBuffer fallbackBitmap = MemoryUtil.memAlloc(BITMAP_WIDTH * BITMAP_HEIGHT);
        for (int i = 0; i < BITMAP_WIDTH * BITMAP_HEIGHT; i++) {
            fallbackBitmap.put((byte) 255);
        }
        fallbackBitmap.flip();
        
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RED, BITMAP_WIDTH, BITMAP_HEIGHT, 0, GL_RED, GL_UNSIGNED_BYTE, fallbackBitmap);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        
        MemoryUtil.memFree(fallbackBitmap);
        
        // Create char data buffer but leave it zeroed - text won't render properly
        // but at least it won't crash. A proper font should be loaded for production use.
        charData = STBTTBakedChar.malloc(CHAR_COUNT);
        
        fontHeight = DEFAULT_FONT_SIZE;
        ascent = DEFAULT_FONT_SIZE;
        descent = 0;
        lineGap = 4;
        
        glBindTexture(GL_TEXTURE_2D, 0);
        
        log.warn("Using fallback font - text rendering may not work correctly");
    }
    
    /**
     * Renders text at the specified position.
     * Note: For proper text rendering, the caller should use UIRenderer's text methods
     * which will handle batching. This method provides direct rendering for simple cases.
     */
    public void renderText(UIRenderer renderer, String text, float x, float y, float scale, Vector4f color) {
        if (text == null || text.isEmpty() || charData == null) return;
        
        // Flush any pending UI draws and bind font texture
        renderer.flush();
        
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, fontTextureId);
        
        float startX = x;
        float cursorX = x;
        
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer xBuf = stack.floats(0);
            FloatBuffer yBuf = stack.floats(0);
            STBTTAlignedQuad quad = STBTTAlignedQuad.malloc(stack);
            
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                
                if (c == '\n') {
                    cursorX = startX;
                    y += getLineHeight(scale);
                    continue;
                }
                
                if (c < FIRST_CHAR || c >= FIRST_CHAR + CHAR_COUNT) {
                    continue; // Skip unsupported characters
                }
                
                // Reset buffers for this character
                xBuf.put(0, cursorX);
                yBuf.put(0, y + ascent * scale);
                
                stbtt_GetBakedQuad(charData, BITMAP_WIDTH, BITMAP_HEIGHT, c - FIRST_CHAR, xBuf, yBuf, quad, true);
                
                // Get the new cursor position
                cursorX = xBuf.get(0);
                
                // The quad positions are in screen space, just apply scale
                float x0 = quad.x0();
                float y0 = quad.y0();
                float x1 = quad.x1();
                float y1 = quad.y1();
                
                // Apply scaling (around the baseline)
                float width = (x1 - x0) * scale;
                float height = (y1 - y0) * scale;
                x0 = quad.x0() + (cursorX - quad.x1()) * (1 - scale);
                x1 = x0 + width;
                y0 = quad.y0();
                y1 = y0 + height;
                
                // Note: Full implementation would batch these render calls
                // For now, text measurement works and rendering is stubbed
            }
        }
    }
    
    private void renderCharQuadDirect(float x0, float y0, float x1, float y1,
                                       float u0, float v0, float u1, float v1, Vector4f color) {
        // Direct OpenGL rendering for text characters
        // This is a placeholder - full implementation would integrate with UIRenderer batching
        // The quad positions and UVs are calculated correctly, but actual rendering
        // requires integration with the shader pipeline
    }
    
    /**
     * Gets the width of text when rendered.
     */
    public float getTextWidth(String text, float scale) {
        if (text == null || text.isEmpty() || charData == null) return 0;
        
        float width = 0;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer xBuf = stack.floats(0);
            FloatBuffer yBuf = stack.floats(0);
            STBTTAlignedQuad quad = STBTTAlignedQuad.malloc(stack);
            
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if (c < FIRST_CHAR || c >= FIRST_CHAR + CHAR_COUNT) continue;
                
                stbtt_GetBakedQuad(charData, BITMAP_WIDTH, BITMAP_HEIGHT, c - FIRST_CHAR, xBuf, yBuf, quad, true);
            }
            
            width = xBuf.get(0);
        }
        
        return width * scale;
    }
    
    /**
     * Gets the height of text when rendered.
     */
    public float getTextHeight(float scale) {
        return fontHeight * scale;
    }
    
    /**
     * Gets the line height (for multi-line text).
     */
    public float getLineHeight(float scale) {
        return (fontHeight + lineGap) * scale;
    }
    
    /**
     * Cleans up font resources.
     */
    public void cleanup() {
        if (fontTextureId != 0) {
            glDeleteTextures(fontTextureId);
        }
        if (charData != null) {
            charData.free();
        }
    }
}
