package engine.services.audio;

import lombok.Getter;
import org.joml.Vector3f;

import static org.lwjgl.openal.AL10.*;

/** OpenAL audio source for playing AudioBuffers. */
public final class AudioSource implements AutoCloseable {

  private final int sourceId;
  /** true if this source has been closed. */
  @Getter
  private boolean closed = false;

  public AudioSource() {
    this.sourceId = alGenSources();

    if (alGetError() != AL_NO_ERROR) {
      throw new RuntimeException("Failed to create OpenAL audio source");
    }

    // Defaults
    setVolume(1.0f);
    setPitch(1.0f);
    setPosition(0.0f, 0.0f, 0.0f);
    setLooping(false);
  }

  /** Bind a buffer and start playback. */
  public void play(AudioBuffer buffer) {
    if (closed) {
      throw new IllegalStateException("AudioSource has been closed");
    }

    alSourcei(sourceId, AL_BUFFER, buffer.getBufferId());
    alSourcePlay(sourceId);
  }

  /** Pause playback. */
  public void pause() {
    if (closed) {
      throw new IllegalStateException("AudioSource has been closed");
    }

    alSourcePause(sourceId);
  }

  /** Resume paused playback. */
  public void resume() {
    if (closed) {
      throw new IllegalStateException("AudioSource has been closed");
    }

    alSourcePlay(sourceId);
  }

  /** Stop playback and rewind. */
  public void stop() {
    if (closed) {
      throw new IllegalStateException("AudioSource has been closed");
    }

    alSourceStop(sourceId);
  }

  /** Set volume (0..1). */
  public void setVolume(float volume) {
    if (closed) {
      throw new IllegalStateException("AudioSource has been closed");
    }

    alSourcef(sourceId, AL_GAIN, Math.max(0.0f, volume));
  }

  /** Set pitch multiplier (>= 0.1). */
  public void setPitch(float pitch) {
    if (closed) {
      throw new IllegalStateException("AudioSource has been closed");
    }

    alSourcef(sourceId, AL_PITCH, Math.max(0.1f, pitch));
  }

  /** Set 3D position. */
  public void setPosition(float x, float y, float z) {
    if (closed) {
      throw new IllegalStateException("AudioSource has been closed");
    }

    alSource3f(sourceId, AL_POSITION, x, y, z);
  }

  /** Set 3D position. */
  public void setPosition(Vector3f position) {
    setPosition(position.x, position.y, position.z);
  }

  /** Enable/disable looping. */
  public void setLooping(boolean looping) {
    if (closed) {
      throw new IllegalStateException("AudioSource has been closed");
    }

    alSourcei(sourceId, AL_LOOPING, looping ? AL_TRUE : AL_FALSE);
  }

  /** Get raw OpenAL playback state. */
  public int getState() {
    if (closed) {
      throw new IllegalStateException("AudioSource has been closed");
    }

    return alGetSourcei(sourceId, AL_SOURCE_STATE);
  }

  /** true if playing. */
  public boolean isPlaying() {
    return getState() == AL_PLAYING;
  }

  /** true if paused. */
  public boolean isPaused() {
    return getState() == AL_PAUSED;
  }

  /** true if stopped. */
  public boolean isStopped() {
    return getState() == AL_STOPPED;
  }

  /** Current volume. */
  public float getVolume() {
    if (closed) {
      throw new IllegalStateException("AudioSource has been closed");
    }

    return alGetSourcef(sourceId, AL_GAIN);
  }

  @Override
  public void close() {
    if (!closed) {
      alDeleteSources(sourceId);
      closed = true;
    }
  }

}
