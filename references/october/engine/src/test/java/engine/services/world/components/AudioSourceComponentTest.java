package engine.services.world.components;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AudioSourceComponentTest {

    @Test
    void constructor_shouldSetHandleAndDefaults() {
        // Given: The component is created with the only valid constructor
        AudioSourceComponent audio = new AudioSourceComponent("test_sound");

        // Then: The handle should be set and all other fields should have their correct default values
        assertEquals("test_sound", audio.audioBufferHandle);
        assertEquals(1.0f, audio.volume);
        assertEquals(1.0f, audio.pitch);
        assertFalse(audio.looping);
        assertFalse(audio.autoPlay);
        assertFalse(audio.isPlaying);
    }

    @Test
    void volume_canBeModified() {
        AudioSourceComponent audio = new AudioSourceComponent("test_sound");

        audio.volume = 0.5f;
        assertEquals(0.5f, audio.volume);

        audio.volume = 0.0f;
        assertEquals(0.0f, audio.volume);
    }

    @Test
    void pitch_canBeModified() {
        AudioSourceComponent audio = new AudioSourceComponent("test_sound");

        audio.pitch = 1.5f;
        assertEquals(1.5f, audio.pitch);

        audio.pitch = 0.5f;
        assertEquals(0.5f, audio.pitch);
    }

    @Test
    void looping_canBeToggled() {
        AudioSourceComponent audio = new AudioSourceComponent("test_sound");

        audio.looping = true;
        assertTrue(audio.looping);

        audio.looping = false;
        assertFalse(audio.looping);
    }

    @Test
    void autoPlay_canBeToggled() {
        AudioSourceComponent audio = new AudioSourceComponent("test_sound");

        audio.autoPlay = true;
        assertTrue(audio.autoPlay);

        audio.autoPlay = false;
        assertFalse(audio.autoPlay);
    }

    @Test
    void isPlaying_canBeSet() {
        AudioSourceComponent audio = new AudioSourceComponent("test_sound");

        audio.isPlaying = true;
        assertTrue(audio.isPlaying);

        audio.isPlaying = false;
        assertFalse(audio.isPlaying);
    }

    @Test
    void constructor_shouldAcceptNullHandle() {
        // While not recommended, the constructor doesn't validate null
        AudioSourceComponent audio = new AudioSourceComponent(null);

        assertNull(audio.audioBufferHandle);
        assertEquals(1.0f, audio.volume);
        assertEquals(1.0f, audio.pitch);
    }
}
