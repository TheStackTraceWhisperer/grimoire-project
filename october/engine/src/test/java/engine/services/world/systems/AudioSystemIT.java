package engine.services.world.systems;

import engine.EngineTestHarness;
import engine.services.audio.AudioSource;
import engine.services.resources.AssetCacheService;
import engine.services.world.World;
import engine.services.world.WorldService;
import engine.services.world.components.MusicComponent;
import engine.services.world.components.SoundEffectComponent;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class AudioSystemIT extends EngineTestHarness {

    @Inject
    private WorldService worldService;
    @Inject
    private AssetCacheService assetCacheService;
    @Inject
    private AudioSystem audioSystem;

    @BeforeEach
    void setUp() {
        worldService.addSystem(audioSystem);
        assetCacheService.loadAudioBuffer("test-music", "audio/test-music.ogg");
        assetCacheService.loadAudioBuffer("test-click", "audio/test-click.ogg");
    }

    @AfterEach
    void tearDown() {
        audioSystem.stopAll();
        worldService.clearSystems();
        worldService.getEntitiesWith().forEach(worldService::destroyEntity);
    }

    @Test
    void testMusicComponent_autoPlayAndLooping() {
        // Given
        int entity = worldService.createEntity();
        MusicComponent music = new MusicComponent("test-music");
        music.looping = true;
        music.autoPlay = true;
        worldService.addComponent(entity, music);

        // When
        tick();

        // Then
        assertTrue(music.isPlaying, "MusicComponent should be playing after one tick.");

        // When we tick again, it should still be playing because it's looping
        tick();
        assertTrue(music.isPlaying, "Looping music should still be playing after a second tick.");
    }

    @Test
    void testSystem_cleansUpSourceWhenComponentIsRemoved() throws Exception {
        // Given: An entity with music that is playing
        int entity = worldService.createEntity();
        worldService.addComponent(entity, new MusicComponent("test-music"));
        tick(); // Tick once to create and play the internal AudioSource

        // And: We can see the internal source was created
        Map<Integer, AudioSource> musicSourceMap = getInternalMusicSourceMap();
        assertFalse(musicSourceMap.isEmpty(), "Internal music source should be created.");
        AudioSource source = musicSourceMap.get(entity);
        assertNotNull(source, "AudioSource should exist in the system's map.");
        assertFalse(source.isClosed(), "AudioSource should be open.");

        // When: The component is removed from the entity
        worldService.removeComponent(entity, MusicComponent.class);
        tick(); // Tick again to allow the system's cleanup logic to run

        // Then: The internal map should be empty and the source should be closed
        assertTrue(musicSourceMap.isEmpty(), "Internal music source should be removed after component is removed.");
        assertTrue(source.isClosed(), "AudioSource should be closed after component is removed.");
    }

    @Test
    void testPauseAndResumeAllMusic() throws Exception {
        // Given: An entity with music that is playing
        int entity = worldService.createEntity();
        worldService.addComponent(entity, new MusicComponent("test-music"));
        tick(); // Start playback

        Map<Integer, AudioSource> musicSourceMap = getInternalMusicSourceMap();
        AudioSource source = musicSourceMap.get(entity);
        assertTrue(source.isPlaying(), "Source should be playing initially.");

        // When: We pause all music
        audioSystem.pauseAllMusic();

        // Then: The underlying source should be paused
        assertTrue(source.isPaused(), "Source should be paused.");

        // When: We resume all music
        audioSystem.resumeAllMusic();

        // Then: The underlying source should be playing again
        assertTrue(source.isPlaying(), "Source should be playing again after resume.");
    }

    @Test
    void testMusicFadeInThenFadeOutStopsPlayback() throws Exception {
        int entity = worldService.createEntity();
        MusicComponent music = new MusicComponent("test-music");
        music.baseVolume = 0.8f;
        music.fadeDuration = 0.01f; // very short fade for CI environments with tiny dt
        music.autoPlay = true;
        music.looping = true;
        worldService.addComponent(entity, music);

        // Trigger creation and fade-in
        tick();
        assertTrue(music.fadingIn, "Music should be fading in after autoPlay.");

        float lastVolume = music.currentVolume;
        // Progress frames until fade-in completes (allow dt to accumulate)
        int safety = 5000;
        while (music.fadingIn && safety-- > 0) {
            tick();
            // tiny sleep to ensure non-zero delta time in fast CI loops
            Thread.sleep(1);
            assertTrue(music.currentVolume >= lastVolume - 0.001f, "Volume should not decrease during fade-in.");
            lastVolume = music.currentVolume;
        }
        assertFalse(music.fadingIn, "Fade-in should complete.");
        assertEquals(music.baseVolume, music.currentVolume, 0.1f, "Volume should be at base after fade-in.");

        // Now start fade-out and ensure it reaches zero and stops
        music.startFadeOut();
        safety = 5000;
        lastVolume = music.currentVolume;
        while (music.fadingOut && safety-- > 0) {
            tick();
            Thread.sleep(1);
            assertTrue(music.currentVolume <= lastVolume + 0.001f, "Volume should not increase during fade-out.");
            lastVolume = music.currentVolume;
        }
        assertFalse(music.fadingOut, "Fade-out should complete.");
        assertEquals(0.0f, music.currentVolume, 0.1f, "Volume should be ~0 after fade-out.");
        // The system should stop playback after fade-out completes
        Map<Integer, AudioSource> musicSourceMap = getInternalMusicSourceMap();
        AudioSource source = musicSourceMap.get(entity);
        assertTrue(source.isStopped() || !music.isPlaying, "Source should be stopped or flag cleared after fade-out.");
    }

    @Test
    void testPlaySoundEffectHelper_setsVolumeAndCleansUpAfterStop() throws Exception {
        int entity = worldService.createEntity();
        World internalWorld = getInternalWorld();

        // When: use helper to add a one-shot sound effect with custom volume
        audioSystem.playSoundEffect(internalWorld, entity, "test-click", new TestSoundEffect(), 0.42f);

        // Then: the component should exist with that volume
        SoundEffectComponent comp = worldService.getComponent(entity, SoundEffectComponent.class);
        assertNotNull(comp, "SoundEffectComponent should be added by helper");
        assertEquals(0.42f, comp.volume, 0.001f);

        // Tick to create the source and trigger playback
        tick();

        // Access internal map, stop the source manually to simulate playback end
        Map<Integer, AudioSource> effectMap = getInternalEffectSourceMap();
        AudioSource s = effectMap.get(entity);
        assertNotNull(s, "Effect AudioSource should be created");

        s.stop();
        // Mark as triggered and ensure removeAfterPlay is true by default; next tick should clean up
        comp.hasBeenTriggered = true;
        comp.removeAfterPlay = true;
        tick();

        // Component should be removed and source entry cleared from the map
        assertNull(worldService.getComponent(entity, SoundEffectComponent.class), "SoundEffectComponent should be removed after stop");
        assertFalse(getInternalEffectSourceMap().containsKey(entity), "Effect source should be removed from internal map after cleanup");
    }

    /**
     * Uses reflection to access the private source map for verification.
     * This is an acceptable testing practice for verifying internal state management.
     */
    @SuppressWarnings("unchecked")
    private Map<Integer, AudioSource> getInternalMusicSourceMap() throws NoSuchFieldException, IllegalAccessException {
        Field field = AudioSystem.class.getDeclaredField("musicSourceMap");
        field.setAccessible(true);
        return (Map<Integer, AudioSource>) field.get(audioSystem);
    }

    @SuppressWarnings("unchecked")
    private Map<Integer, AudioSource> getInternalEffectSourceMap() throws NoSuchFieldException, IllegalAccessException {
        Field field = AudioSystem.class.getDeclaredField("soundEffectSourceMap");
        field.setAccessible(true);
        return (Map<Integer, AudioSource>) field.get(audioSystem);
    }

    private World getInternalWorld() throws NoSuchFieldException, IllegalAccessException {
        Field f = WorldService.class.getDeclaredField("world");
        f.setAccessible(true);
        return (World) f.get(worldService);
    }

    private static class TestSoundEffect implements SoundEffectComponent.SoundEffectType { }
}
