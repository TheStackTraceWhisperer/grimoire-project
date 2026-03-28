package engine.services.world.components;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SoundEffectComponentTest {

    // Mock implementation for testing
    private enum TestSoundEffectType implements SoundEffectComponent.SoundEffectType {
        EXPLOSION, JUMP, COLLECT
    }

    @Test
    void constructor_shouldSetRequiredFieldsAndDefaults() {
        SoundEffectComponent sound = new SoundEffectComponent("explosion_sound", TestSoundEffectType.EXPLOSION);

        assertEquals("explosion_sound", sound.soundBufferHandle);
        assertEquals(TestSoundEffectType.EXPLOSION, sound.soundType);
        assertEquals(1.0f, sound.volume);
        assertEquals(1.0f, sound.pitch);
        assertTrue(sound.autoPlay);
        assertTrue(sound.removeAfterPlay);
        assertFalse(sound.hasBeenTriggered);
    }

    @Test
    void volume_canBeModified() {
        SoundEffectComponent sound = new SoundEffectComponent("test_sound", TestSoundEffectType.JUMP);

        sound.volume = 0.5f;
        assertEquals(0.5f, sound.volume);

        sound.volume = 0.0f;
        assertEquals(0.0f, sound.volume);
    }

    @Test
    void pitch_canBeModified() {
        SoundEffectComponent sound = new SoundEffectComponent("test_sound", TestSoundEffectType.JUMP);

        sound.pitch = 1.5f;
        assertEquals(1.5f, sound.pitch);

        sound.pitch = 0.5f;
        assertEquals(0.5f, sound.pitch);
    }

    @Test
    void autoPlay_canBeModified() {
        SoundEffectComponent sound = new SoundEffectComponent("test_sound", TestSoundEffectType.JUMP);

        sound.autoPlay = false;
        assertFalse(sound.autoPlay);

        sound.autoPlay = true;
        assertTrue(sound.autoPlay);
    }

    @Test
    void removeAfterPlay_canBeModified() {
        SoundEffectComponent sound = new SoundEffectComponent("test_sound", TestSoundEffectType.JUMP);

        sound.removeAfterPlay = false;
        assertFalse(sound.removeAfterPlay);

        sound.removeAfterPlay = true;
        assertTrue(sound.removeAfterPlay);
    }

    @Test
    void hasBeenTriggered_defaultsToFalse() {
        SoundEffectComponent sound = new SoundEffectComponent("test_sound", TestSoundEffectType.COLLECT);

        assertFalse(sound.hasBeenTriggered);
    }

    @Test
    void hasBeenTriggered_canBeSet() {
        SoundEffectComponent sound = new SoundEffectComponent("test_sound", TestSoundEffectType.COLLECT);

        sound.hasBeenTriggered = true;
        assertTrue(sound.hasBeenTriggered);
    }

    @Test
    void constructor_shouldAcceptNullSoundType() {
        SoundEffectComponent sound = new SoundEffectComponent("test_sound", null);

        assertEquals("test_sound", sound.soundBufferHandle);
        assertNull(sound.soundType);
    }
}
