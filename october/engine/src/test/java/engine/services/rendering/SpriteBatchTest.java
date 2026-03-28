package engine.services.rendering;

import org.joml.Matrix4f;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SpriteBatchTest {

    private SpriteBatch spriteBatch;

    @Mock
    private Texture texture1;

    @Mock
    private Texture texture2;

    @BeforeEach
    void setUp() {
        spriteBatch = new SpriteBatch();
    }

    @Test
    void testAddSpriteGroupsByTexture() {
        // Given
        Matrix4f transform1 = new Matrix4f().identity();
        Matrix4f transform2 = new Matrix4f().translate(1, 0, 0);
        Matrix4f transform3 = new Matrix4f().translate(2, 0, 0);

        // When
        spriteBatch.addSprite(texture1, transform1);
        spriteBatch.addSprite(texture2, transform2);
        spriteBatch.addSprite(texture1, transform3);

        // Then
        assertEquals(2, spriteBatch.getSpriteCount(texture1));
        assertEquals(1, spriteBatch.getSpriteCount(texture2));
        assertEquals(2, spriteBatch.getBatchCount());
        assertEquals(3, spriteBatch.getTotalSpriteCount());
    }

    @Test
    void testClearRemovesAllSpritesAndBatches() {
        // Given a non-empty batch
        spriteBatch.addSprite(texture1, new Matrix4f());
        assertFalse(spriteBatch.isEmpty());

        // When
        spriteBatch.clear();

        // Then
        assertTrue(spriteBatch.isEmpty());
        assertEquals(0, spriteBatch.getTotalSpriteCount());
        assertEquals(0, spriteBatch.getBatchCount());
    }

    @Test
    void testGetSpritesForTextureReturnsCorrectTransforms() {
        // Given
        Matrix4f transform1 = new Matrix4f().identity();
        Matrix4f transform2 = new Matrix4f().translate(1, 0, 0);
        spriteBatch.addSprite(texture1, transform1);
        spriteBatch.addSprite(texture1, transform2);

        // When
        var transforms = spriteBatch.getSpritesForTexture(texture1);

        // Then
        assertEquals(2, transforms.size());
        assertTrue(transforms.contains(transform1));
        assertTrue(transforms.contains(transform2));
    }

    @Test
    void testIsEmptyReturnsCorrectState() {
        // Given an empty batch
        assertTrue(spriteBatch.isEmpty());

        // When a sprite is added
        spriteBatch.addSprite(texture1, new Matrix4f());

        // Then it is no longer empty
        assertFalse(spriteBatch.isEmpty());
    }
}
