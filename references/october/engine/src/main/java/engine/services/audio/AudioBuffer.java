package engine.services.audio;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import static org.lwjgl.openal.AL10.AL_FORMAT_MONO16;
import static org.lwjgl.openal.AL10.AL_FORMAT_STEREO16;
import static org.lwjgl.openal.AL10.AL_NO_ERROR;
import static org.lwjgl.openal.AL10.alBufferData;
import static org.lwjgl.openal.AL10.alDeleteBuffers;
import static org.lwjgl.openal.AL10.alGenBuffers;
import static org.lwjgl.openal.AL10.alGetError;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_decode_memory;

/** Immutable OpenAL audio buffer. */
public final class AudioBuffer implements AutoCloseable {

  private final int bufferId;
  private boolean closed = false;

  /** Create from PCM16 data. */
  public AudioBuffer(ShortBuffer data, int channels, int sampleRate) {
    this.bufferId = alGenBuffers();

    int format = channels == 1 ? AL_FORMAT_MONO16 : AL_FORMAT_STEREO16;
    alBufferData(bufferId, format, data, sampleRate);

    if (alGetError() != AL_NO_ERROR) {
      throw new RuntimeException("Failed to upload audio data to OpenAL buffer");
    }
  }

  /** Load from a classpath OGG file. */
  public static AudioBuffer loadFromOggFile(String resourcePath) {
    try (MemoryStack stack = MemoryStack.stackPush()) {
      ByteBuffer oggData = loadResourceAsBuffer(resourcePath);
      if (oggData == null) {
        throw new RuntimeException("Failed to load audio file: " + resourcePath);
      }

      IntBuffer channelsBuffer = stack.mallocInt(1);
      IntBuffer sampleRateBuffer = stack.mallocInt(1);

      ShortBuffer audioData = stb_vorbis_decode_memory(oggData, channelsBuffer, sampleRateBuffer);
      if (audioData == null) {
        throw new RuntimeException("Failed to decode OGG file: " + resourcePath);
      }

      int channels = channelsBuffer.get(0);
      int sampleRate = sampleRateBuffer.get(0);

      return new AudioBuffer(audioData, channels, sampleRate);
    }
  }

  /** Load a classpath resource into a ByteBuffer. */
  private static ByteBuffer loadResourceAsBuffer(String resourcePath) {
    String correctedPath = resourcePath.startsWith("/") ? resourcePath : "/" + resourcePath;

    try (var inputStream = AudioBuffer.class.getResourceAsStream(correctedPath)) {
      if (inputStream == null) {
        return null;
      }

      byte[] bytes = inputStream.readAllBytes();
      ByteBuffer buffer = MemoryUtil.memAlloc(bytes.length);
      buffer.put(bytes);
      buffer.flip();
      return buffer;
    } catch (Exception e) {
      throw new RuntimeException("Failed to load audio resource: " + resourcePath, e);
    }
  }

  /** OpenAL buffer id. */
  public int getBufferId() {
    if (closed) {
      throw new IllegalStateException("AudioBuffer has been closed");
    }
    return bufferId;
  }

  @Override
  public void close() {
    if (!closed) {
      alDeleteBuffers(bufferId);
      closed = true;
    }
  }

  /** true if closed. */
  public boolean isClosed() {
    return closed;
  }
}
