package engine.services.rendering;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Defines an animation sequence as a series of sprite frame names with timing information.
 * <p>
 * An animation consists of multiple frames, each referencing a region name in a sprite sheet.
 * The animation can be configured to loop and has a frame duration that controls playback speed.
 * <p>
 * Example YML configuration:
 * <pre>
 * animations:
 *   walk_down:
 *     frames: [walk_down_0, walk_down_1, walk_down_2, walk_down_3]
 *     frameDuration: 0.15
 *     loop: true
 * </pre>
 */
public class SpriteAnimation {

  private final String name;
  private final List<String> frameNames;
  private final float frameDuration;
  private final boolean loop;

  /**
   * Creates a new sprite animation.
   *
   * @param name The unique name of this animation
   * @param frameNames List of sprite region names that make up this animation
   * @param frameDuration Duration in seconds for each frame
   * @param loop Whether the animation should loop when it reaches the end
   */
  public SpriteAnimation(String name, List<String> frameNames, float frameDuration, boolean loop) {
    this.name = Objects.requireNonNull(name, "name must not be null");
    Objects.requireNonNull(frameNames, "frameNames must not be null");
    if (frameNames.isEmpty()) {
      throw new IllegalArgumentException("frameNames must not be empty");
    }
    if (frameDuration <= 0) {
      throw new IllegalArgumentException("frameDuration must be positive, got: " + frameDuration);
    }
    this.frameNames = new ArrayList<>(frameNames);
    this.frameDuration = frameDuration;
    this.loop = loop;
  }

  /**
   * Gets the animation name.
   *
   * @return The animation name
   */
  public String getName() {
    return name;
  }

  /**
   * Gets the frame names for this animation.
   *
   * @return An unmodifiable list of frame names
   */
  public List<String> getFrameNames() {
    return Collections.unmodifiableList(frameNames);
  }

  /**
   * Gets the duration of each frame in seconds.
   *
   * @return The frame duration
   */
  public float getFrameDuration() {
    return frameDuration;
  }

  /**
   * Checks if this animation loops.
   *
   * @return True if the animation loops
   */
  public boolean isLoop() {
    return loop;
  }

  /**
   * Gets the total number of frames in this animation.
   *
   * @return The frame count
   */
  public int getFrameCount() {
    return frameNames.size();
  }

  /**
   * Gets the total duration of the animation in seconds.
   *
   * @return The total animation duration
   */
  public float getTotalDuration() {
    return frameNames.size() * frameDuration;
  }

  /**
   * Gets the frame name at the specified index.
   *
   * @param frameIndex The frame index (0-based)
   * @return The frame name at that index
   * @throws IndexOutOfBoundsException if the index is invalid
   */
  public String getFrameName(int frameIndex) {
    return frameNames.get(frameIndex);
  }

  /**
   * Calculates which frame should be displayed at the given time.
   *
   * @param elapsedTime The elapsed time since the animation started
   * @return The frame index to display
   */
  public int getFrameIndex(float elapsedTime) {
    if (elapsedTime < 0) {
      return 0;
    }

    int totalFrames = frameNames.size();
    int frameIndex = (int) (elapsedTime / frameDuration);

    if (loop) {
      return frameIndex % totalFrames;
    } else {
      return Math.min(frameIndex, totalFrames - 1);
    }
  }
}
