package engine.services.audio;

import engine.EngineTestHarness;
import engine.services.resources.AssetCacheService;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AudioServiceIT extends EngineTestHarness {

    @Inject
    private AudioService audioService;
    @Inject
    private AssetCacheService assetCacheService;

    @Test
    void testAudioPlaybackLifecycle() {
        // Given: The AudioService is initialized by the EngineTestHarness
        assertTrue(audioService.isInitialized(), "AudioService should be initialized");

        // And: A valid audio buffer is loaded
        AudioBuffer soundBuffer = assetCacheService.loadAudioBuffer("test-sound", "audio/test-sound.ogg");
        assertNotNull(soundBuffer, "AudioBuffer should be loaded successfully");

        // When: We create an audio source and play the buffer
        try (AudioSource source = audioService.createSource()) {
            assertNotNull(source, "AudioSource should be created successfully");

            source.play(soundBuffer);

            // Then: The source should report that it is playing
            // In a headless environment with a null driver, the state changes are immediate.
            assertTrue(source.isPlaying(), "AudioSource should be in the PLAYING state");

            // When: We stop the source
            source.stop();

            // Then: The source should report that it is stopped
            assertTrue(source.isStopped(), "AudioSource should be in the STOPPED state");
        } // The try-with-resources block ensures source.close() is called
    }

    @Test
    void testMasterVolumeControl() {
        // Given: The AudioService is initialized
        assertTrue(audioService.isInitialized());
        float initialVolume = audioService.getMasterVolume();

        // When: We set the master volume to a new value
        float newVolume = 0.5f;
        audioService.setMasterVolume(newVolume);

        // Then: The master volume should be updated
        assertEquals(newVolume, audioService.getMasterVolume(), 0.001f, "Master volume should be updated");

        // Reset volume for other tests
        audioService.setMasterVolume(initialVolume);
    }

    @Test
    void testMasterVolumeClampedAtZeroWhenNegative() {
        assertTrue(audioService.isInitialized());
        audioService.setMasterVolume(-1.0f);
        assertEquals(0.0f, audioService.getMasterVolume(), 0.001f, "Negative master volume should clamp to 0.0");
    }

    @Test
    void testSourceVolumeClampAndStates() {
        assertTrue(audioService.isInitialized());
        // Load a small buffer and exercise volume clamp
        AudioBuffer soundBuffer = assetCacheService.loadAudioBuffer("test-sound-2", "audio/test-sound.ogg");
        assertNotNull(soundBuffer);

        try (AudioSource source = audioService.createSource()) {
            source.setVolume(-0.5f);
            assertEquals(0.0f, source.getVolume(), 0.001f, "Negative source volume should clamp to 0.0");

            source.setLooping(false);
            source.play(soundBuffer);
            assertTrue(source.isPlaying());
            source.pause();
            assertTrue(source.isPaused());
            source.resume();
            assertTrue(source.isPlaying());
            source.stop();
            assertTrue(source.isStopped());
        }
    }

    @Test
    void testAudioBufferCloseThenGetIdThrows() {
        assertTrue(audioService.isInitialized());
        // Use a dedicated handle to avoid polluting cache used by other tests
        AudioBuffer soundBuffer = assetCacheService.loadAudioBuffer("close-me", "audio/test-sound.ogg");
        assertNotNull(soundBuffer);
        soundBuffer.close();
        assertTrue(soundBuffer.isClosed(), "Buffer should report closed after close()");
        assertThrows(IllegalStateException.class, soundBuffer::getBufferId, "getBufferId after close should throw");
    }
}
