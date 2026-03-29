package engine.services.world.components;

import engine.services.rendering.Direction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AnimatedSpriteComponentTest {

  @Test
  void constructor_shouldCreateValidComponent() {
    AnimatedSpriteComponent component = new AnimatedSpriteComponent(
        "player_sheet", "walk", Direction.DOWN, 0.5f, true);

    assertEquals("player_sheet", component.spriteSheetHandle());
    assertEquals("walk", component.currentAnimation());
    assertEquals(Direction.DOWN, component.currentDirection());
    assertEquals(0.5f, component.animationTime());
    assertTrue(component.playing());
  }

  @Test
  void constructor_shouldThrowExceptionForNullSpriteSheetHandle() {
    assertThrows(NullPointerException.class, () ->
        new AnimatedSpriteComponent(null, "walk", Direction.DOWN, 0.0f, true));
  }

  @Test
  void constructor_shouldThrowExceptionForNullAnimation() {
    assertThrows(NullPointerException.class, () ->
        new AnimatedSpriteComponent("sheet", null, Direction.DOWN, 0.0f, true));
  }

  @Test
  void constructor_shouldThrowExceptionForNullDirection() {
    assertThrows(NullPointerException.class, () ->
        new AnimatedSpriteComponent("sheet", "walk", null, 0.0f, true));
  }

  @Test
  void constructor_shouldThrowExceptionForNegativeAnimationTime() {
    assertThrows(IllegalArgumentException.class, () ->
        new AnimatedSpriteComponent("sheet", "walk", Direction.DOWN, -0.1f, true));
  }

  @Test
  void simpleConstructor_shouldSetDefaultValues() {
    AnimatedSpriteComponent component = new AnimatedSpriteComponent("player_sheet", "idle");

    assertEquals("player_sheet", component.spriteSheetHandle());
    assertEquals("idle", component.currentAnimation());
    assertEquals(Direction.DOWN, component.currentDirection());
    assertEquals(0.0f, component.animationTime());
    assertTrue(component.playing());
  }

  @Test
  void withAnimation_shouldCreateNewComponentWithDifferentAnimation() {
    AnimatedSpriteComponent original = new AnimatedSpriteComponent(
        "sheet", "walk", Direction.LEFT, 1.5f, true);

    AnimatedSpriteComponent updated = original.withAnimation("run");

    assertEquals("run", updated.currentAnimation());
    assertEquals("sheet", updated.spriteSheetHandle());
    assertEquals(Direction.LEFT, updated.currentDirection());
    assertEquals(0.0f, updated.animationTime()); // Time should reset
    assertTrue(updated.playing());

    // Original should be unchanged
    assertEquals("walk", original.currentAnimation());
  }

  @Test
  void withDirection_shouldCreateNewComponentWithDifferentDirection() {
    AnimatedSpriteComponent original = new AnimatedSpriteComponent(
        "sheet", "walk", Direction.DOWN, 1.0f, true);

    AnimatedSpriteComponent updated = original.withDirection(Direction.UP);

    assertEquals(Direction.UP, updated.currentDirection());
    assertEquals("walk", updated.currentAnimation());
    assertEquals(1.0f, updated.animationTime()); // Time should be preserved

    // Original should be unchanged
    assertEquals(Direction.DOWN, original.currentDirection());
  }

  @Test
  void withTimeAdvanced_shouldAddDeltaTimeToAnimationTime() {
    AnimatedSpriteComponent original = new AnimatedSpriteComponent(
        "sheet", "walk", Direction.DOWN, 1.0f, true);

    AnimatedSpriteComponent updated = original.withTimeAdvanced(0.5f);

    assertEquals(1.5f, updated.animationTime(), 0.001f);

    // Original should be unchanged
    assertEquals(1.0f, original.animationTime());
  }

  @Test
  void withPlaying_shouldChangePlayingState() {
    AnimatedSpriteComponent original = new AnimatedSpriteComponent(
        "sheet", "walk", Direction.DOWN, 1.0f, true);

    AnimatedSpriteComponent paused = original.withPlaying(false);

    assertFalse(paused.playing());
    assertEquals(1.0f, paused.animationTime()); // Time should be preserved

    // Original should be unchanged
    assertTrue(original.playing());
  }

  @Test
  void withReset_shouldResetAnimationTime() {
    AnimatedSpriteComponent original = new AnimatedSpriteComponent(
        "sheet", "walk", Direction.DOWN, 5.5f, true);

    AnimatedSpriteComponent reset = original.withReset();

    assertEquals(0.0f, reset.animationTime());
    assertEquals("walk", reset.currentAnimation());
    assertEquals(Direction.DOWN, reset.currentDirection());
    assertTrue(reset.playing());

    // Original should be unchanged
    assertEquals(5.5f, original.animationTime());
  }

  @Test
  void equals_shouldReturnTrueForIdenticalComponents() {
    AnimatedSpriteComponent component1 = new AnimatedSpriteComponent(
        "sheet", "walk", Direction.DOWN, 1.0f, true);
    AnimatedSpriteComponent component2 = new AnimatedSpriteComponent(
        "sheet", "walk", Direction.DOWN, 1.0f, true);

    assertEquals(component1, component2);
  }

  @Test
  void equals_shouldReturnFalseForDifferentAnimations() {
    AnimatedSpriteComponent component1 = new AnimatedSpriteComponent(
        "sheet", "walk", Direction.DOWN, 1.0f, true);
    AnimatedSpriteComponent component2 = new AnimatedSpriteComponent(
        "sheet", "run", Direction.DOWN, 1.0f, true);

    assertNotEquals(component1, component2);
  }

  @Test
  void hashCode_shouldBeConsistentForEqualComponents() {
    AnimatedSpriteComponent component1 = new AnimatedSpriteComponent(
        "sheet", "walk", Direction.DOWN, 1.0f, true);
    AnimatedSpriteComponent component2 = new AnimatedSpriteComponent(
        "sheet", "walk", Direction.DOWN, 1.0f, true);

    assertEquals(component1.hashCode(), component2.hashCode());
  }

  @Test
  void withMethods_shouldChainCorrectly() {
    AnimatedSpriteComponent component = new AnimatedSpriteComponent("sheet", "idle")
        .withAnimation("walk")
        .withDirection(Direction.UP)
        .withTimeAdvanced(0.5f)
        .withPlaying(false);

    assertEquals("walk", component.currentAnimation());
    assertEquals(Direction.UP, component.currentDirection());
    assertEquals(0.5f, component.animationTime(), 0.001f);
    assertFalse(component.playing());
  }
}
