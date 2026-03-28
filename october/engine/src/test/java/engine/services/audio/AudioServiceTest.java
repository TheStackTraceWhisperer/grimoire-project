package engine.services.audio;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.nio.ShortBuffer;

import static org.junit.jupiter.api.Assertions.*;

class AudioServiceTest {

  @Test
  void methodsThrowWhenNotInitialized() {
    AudioService audioService = new AudioService();

    // setMasterVolume
    assertThrows(IllegalStateException.class, () -> audioService.setMasterVolume(1.0f));

    // getMasterVolume
    assertThrows(IllegalStateException.class, audioService::getMasterVolume);

    // setListenerPosition
    assertThrows(IllegalStateException.class, () -> audioService.setListenerPosition(0, 0, 0));

    // setListenerOrientation
    assertThrows(IllegalStateException.class, () -> audioService.setListenerOrientation(0, 0, -1, 0, 1, 0));

    // createSource
    assertThrows(IllegalStateException.class, audioService::createSource);

    // createBuffer
    ShortBuffer data = ShortBuffer.allocate(2);
    assertThrows(IllegalStateException.class, () -> audioService.createBuffer(data, 1, 44100));

    // loadAudioBuffer
    assertThrows(IllegalStateException.class, () -> audioService.loadAudioBuffer("audio/test-sound.ogg"));

    // stop should be a no-op when not initialized
    assertDoesNotThrow(audioService::stop);
  }

  @Test
  void startThrowsIfAlreadyInitialized() throws Exception {
    AudioService audioService = new AudioService();

    // Force the initialized flag for this unit test (we must not hit native calls here)
    Field f = AudioService.class.getDeclaredField("initialized");
    f.setAccessible(true);
    f.set(audioService, true);

    IllegalStateException ex = assertThrows(IllegalStateException.class, audioService::start);
    assertTrue(ex.getMessage().contains("already initialized"));
  }
}
