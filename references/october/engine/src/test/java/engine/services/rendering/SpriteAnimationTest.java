package engine.services.rendering;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SpriteAnimationTest {

  @Test
  void constructor_shouldCreateValidAnimation() {
    List<String> frames = Arrays.asList("frame1", "frame2", "frame3");
    SpriteAnimation animation = new SpriteAnimation("walk", frames, 0.1f, true);

    assertEquals("walk", animation.getName());
    assertEquals(frames, animation.getFrameNames());
    assertEquals(0.1f, animation.getFrameDuration());
    assertTrue(animation.isLoop());
    assertEquals(3, animation.getFrameCount());
  }

  @Test
  void constructor_shouldThrowExceptionForNullName() {
    List<String> frames = Arrays.asList("frame1");
    assertThrows(NullPointerException.class, () ->
        new SpriteAnimation(null, frames, 0.1f, true));
  }

  @Test
  void constructor_shouldThrowExceptionForNullFrameNames() {
    assertThrows(NullPointerException.class, () ->
        new SpriteAnimation("walk", null, 0.1f, true));
  }

  @Test
  void constructor_shouldThrowExceptionForEmptyFrameNames() {
    assertThrows(IllegalArgumentException.class, () ->
        new SpriteAnimation("walk", List.of(), 0.1f, true));
  }

  @Test
  void constructor_shouldThrowExceptionForZeroFrameDuration() {
    List<String> frames = Arrays.asList("frame1");
    assertThrows(IllegalArgumentException.class, () ->
        new SpriteAnimation("walk", frames, 0.0f, true));
  }

  @Test
  void constructor_shouldThrowExceptionForNegativeFrameDuration() {
    List<String> frames = Arrays.asList("frame1");
    assertThrows(IllegalArgumentException.class, () ->
        new SpriteAnimation("walk", frames, -0.1f, true));
  }

  @Test
  void getTotalDuration_shouldCalculateCorrectly() {
    List<String> frames = Arrays.asList("f1", "f2", "f3", "f4");
    SpriteAnimation animation = new SpriteAnimation("walk", frames, 0.2f, true);

    assertEquals(0.8f, animation.getTotalDuration(), 0.001f);
  }

  @Test
  void getFrameName_shouldReturnCorrectFrame() {
    List<String> frames = Arrays.asList("f1", "f2", "f3");
    SpriteAnimation animation = new SpriteAnimation("walk", frames, 0.1f, true);

    assertEquals("f1", animation.getFrameName(0));
    assertEquals("f2", animation.getFrameName(1));
    assertEquals("f3", animation.getFrameName(2));
  }

  @Test
  void getFrameName_shouldThrowExceptionForInvalidIndex() {
    List<String> frames = Arrays.asList("f1", "f2");
    SpriteAnimation animation = new SpriteAnimation("walk", frames, 0.1f, true);

    assertThrows(IndexOutOfBoundsException.class, () -> animation.getFrameName(5));
    assertThrows(IndexOutOfBoundsException.class, () -> animation.getFrameName(-1));
  }

  @Test
  void getFrameIndex_shouldReturnCorrectFrameForTime() {
    List<String> frames = Arrays.asList("f1", "f2", "f3", "f4");
    SpriteAnimation animation = new SpriteAnimation("walk", frames, 0.1f, true);

    assertEquals(0, animation.getFrameIndex(0.05f));
    assertEquals(1, animation.getFrameIndex(0.15f));
    assertEquals(2, animation.getFrameIndex(0.25f));
    assertEquals(3, animation.getFrameIndex(0.35f));
  }

  @Test
  void getFrameIndex_shouldLoopForLoopingAnimation() {
    List<String> frames = Arrays.asList("f1", "f2", "f3");
    SpriteAnimation animation = new SpriteAnimation("walk", frames, 0.1f, true);

    assertEquals(0, animation.getFrameIndex(0.3f)); // Loop back to first frame
    assertEquals(1, animation.getFrameIndex(0.4f)); // Second frame of second loop
    assertEquals(2, animation.getFrameIndex(0.5f)); // Third frame of second loop
  }

  @Test
  void getFrameIndex_shouldClampForNonLoopingAnimation() {
    List<String> frames = Arrays.asList("f1", "f2", "f3");
    SpriteAnimation animation = new SpriteAnimation("attack", frames, 0.1f, false);

    assertEquals(2, animation.getFrameIndex(0.3f)); // Stay on last frame
    assertEquals(2, animation.getFrameIndex(1.0f)); // Stay on last frame
    assertEquals(2, animation.getFrameIndex(10.0f)); // Stay on last frame
  }

  @Test
  void getFrameIndex_shouldReturnZeroForNegativeTime() {
    List<String> frames = Arrays.asList("f1", "f2", "f3");
    SpriteAnimation animation = new SpriteAnimation("walk", frames, 0.1f, true);

    assertEquals(0, animation.getFrameIndex(-0.5f));
  }

  @Test
  void getFrameNames_shouldReturnUnmodifiableList() {
    List<String> frames = Arrays.asList("f1", "f2", "f3");
    SpriteAnimation animation = new SpriteAnimation("walk", frames, 0.1f, true);

    List<String> retrievedFrames = animation.getFrameNames();

    assertThrows(UnsupportedOperationException.class, () -> retrievedFrames.add("f4"));
  }

  @Test
  void constructor_shouldCreateDefensiveCopyOfFrameList() {
    List<String> frames = Arrays.asList("f1", "f2", "f3");
    SpriteAnimation animation = new SpriteAnimation("walk", frames, 0.1f, true);

    // Original list and animation's list should be independent
    assertEquals(frames, animation.getFrameNames());
    assertNotSame(frames, animation.getFrameNames());
  }

  @Test
  void getFrameIndex_shouldHandleExactFrameBoundaries() {
    List<String> frames = Arrays.asList("f1", "f2", "f3");
    SpriteAnimation animation = new SpriteAnimation("walk", frames, 0.1f, true);

    assertEquals(0, animation.getFrameIndex(0.0f));
    assertEquals(1, animation.getFrameIndex(0.1f));
    assertEquals(2, animation.getFrameIndex(0.2f));
  }
}
