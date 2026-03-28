package engine.services.audio;

import engine.IService;
import jakarta.inject.Singleton;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.openal.ALCapabilities;

import static org.lwjgl.openal.AL10.AL_GAIN;
import static org.lwjgl.openal.AL10.AL_ORIENTATION;
import static org.lwjgl.openal.AL10.AL_POSITION;
import static org.lwjgl.openal.AL10.alGetListenerf;
import static org.lwjgl.openal.AL10.alListener3f;
import static org.lwjgl.openal.AL10.alListenerf;
import static org.lwjgl.openal.AL10.alListenerfv;
import static org.lwjgl.openal.ALC10.alcCloseDevice;
import static org.lwjgl.openal.ALC10.alcCreateContext;
import static org.lwjgl.openal.ALC10.alcDestroyContext;
import static org.lwjgl.openal.ALC10.alcMakeContextCurrent;
import static org.lwjgl.openal.ALC10.alcOpenDevice;

/** Manage OpenAL context and basic audio operations. */
@Singleton
@Slf4j
public final class AudioService implements IService {
  private long device;
  private long context;

  @Getter
  private boolean initialized = false;

  @Override
  public int executionOrder() {
    return 10;
  }

  /** Initialize the audio system; idempotent guard. */
  @Override
  public void start() {
    if (initialized) {
      throw new IllegalStateException("AudioService is already initialized");
    }

    try {
      // Open the default audio device
      device = alcOpenDevice((CharSequence) null);
      if (device == 0) {
        log.warn("No OpenAL device available - audio will be disabled");
        return;
      }

      // Create an OpenAL context
      context = alcCreateContext(device, (int[]) null);
      if (context == 0) {
        alcCloseDevice(device);
        log.warn("Failed to create OpenAL context - audio will be disabled");
        return;
      }

      // Make the context current
      if (!alcMakeContextCurrent(context)) {
        alcDestroyContext(context);
        alcCloseDevice(device);
        log.warn("Failed to make OpenAL context current - audio will be disabled");
        return;
      }

      // Initialize OpenAL capabilities
      ALCCapabilities alcCapabilities = ALC.createCapabilities(device);
      ALCapabilities alCapabilities = AL.createCapabilities(alcCapabilities);

      if (!alCapabilities.OpenAL10) {
        log.warn("OpenAL 1.0 is not supported - audio will be disabled");
        return;
      }

      initialized = true;

      // Set up the audio listener at the origin
      setListenerPosition(0.0f, 0.0f, 0.0f);
      setListenerOrientation(0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f);
      setMasterVolume(1.0f);
    } catch (Exception e) {
      log.warn("Audio initialization failed - audio will be disabled", e);
      initialized = false;
    }
  }

  /** Set master volume (0..1). */
  public void setMasterVolume(float volume) {
    if (!initialized) {
      throw new IllegalStateException("AudioService is not initialized");
    }

    alListenerf(AL_GAIN, Math.max(0.0f, volume));
  }

  /** Current master volume. */
  public float getMasterVolume() {
    if (!initialized) {
      throw new IllegalStateException("AudioService is not initialized");
    }

    return alGetListenerf(AL_GAIN);
  }

  /** Set listener position. */
  public void setListenerPosition(float x, float y, float z) {
    if (!initialized) {
      throw new IllegalStateException("AudioService is not initialized");
    }

    alListener3f(AL_POSITION, x, y, z);
  }

  /** Set listener orientation (forward xyz, up xyz). */
  public void setListenerOrientation(float forwardX, float forwardY, float forwardZ,
                                     float upX, float upY, float upZ) {
    if (!initialized) {
      throw new IllegalStateException("AudioService is not initialized");
    }

    float[] orientation = {forwardX, forwardY, forwardZ, upX, upY, upZ};
    alListenerfv(AL_ORIENTATION, orientation);
  }

  /** Create a new AudioSource. */
  public AudioSource createSource() {
    if (!initialized) {
      throw new IllegalStateException("AudioService is not initialized");
    }

    return new AudioSource();
  }

  /** Create an AudioBuffer from PCM16 data. */
  public AudioBuffer createBuffer(java.nio.ShortBuffer data, int channels, int sampleRate) {
    if (!initialized) {
      throw new IllegalStateException("AudioService is not initialized");
    }

    return new AudioBuffer(data, channels, sampleRate);
  }

  /** Load an AudioBuffer from a classpath OGG resource. */
  public AudioBuffer loadAudioBuffer(String resourcePath) {
    if (!initialized) {
      throw new IllegalStateException("AudioService is not initialized");
    }

    return AudioBuffer.loadFromOggFile(resourcePath);
  }

  /** Tear down the audio system and free resources. */
  @Override
  public void stop() {
    if (initialized) {
      // Clean up OpenAL context
      alcMakeContextCurrent(0);
      alcDestroyContext(context);
      alcCloseDevice(device);

      initialized = false;
    }
  }
}
