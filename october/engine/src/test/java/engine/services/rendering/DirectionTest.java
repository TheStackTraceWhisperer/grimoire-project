package engine.services.rendering;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DirectionTest {

  @Test
  void fromAngle_shouldReturnUpFor0Degrees() {
    assertEquals(Direction.UP, Direction.fromAngle(0));
  }

  @Test
  void fromAngle_shouldReturnRightFor90Degrees() {
    assertEquals(Direction.RIGHT, Direction.fromAngle(90));
  }

  @Test
  void fromAngle_shouldReturnDownFor180Degrees() {
    assertEquals(Direction.DOWN, Direction.fromAngle(180));
  }

  @Test
  void fromAngle_shouldReturnLeftFor270Degrees() {
    assertEquals(Direction.LEFT, Direction.fromAngle(270));
  }

  @Test
  void fromAngle_shouldReturnUpRightFor45Degrees() {
    assertEquals(Direction.UP_RIGHT, Direction.fromAngle(45));
  }

  @Test
  void fromAngle_shouldReturnDownRightFor135Degrees() {
    assertEquals(Direction.DOWN_RIGHT, Direction.fromAngle(135));
  }

  @Test
  void fromAngle_shouldReturnDownLeftFor225Degrees() {
    assertEquals(Direction.DOWN_LEFT, Direction.fromAngle(225));
  }

  @Test
  void fromAngle_shouldReturnUpLeftFor315Degrees() {
    assertEquals(Direction.UP_LEFT, Direction.fromAngle(315));
  }

  @Test
  void fromAngle_shouldHandleNegativeAngles() {
    assertEquals(Direction.LEFT, Direction.fromAngle(-90));
    assertEquals(Direction.DOWN, Direction.fromAngle(-180));
  }

  @Test
  void fromAngle_shouldHandleAnglesOver360() {
    assertEquals(Direction.UP, Direction.fromAngle(360));
    assertEquals(Direction.RIGHT, Direction.fromAngle(450));
    assertEquals(Direction.DOWN, Direction.fromAngle(540));
  }

  @Test
  void fromAngle_shouldRoundToNearestDirection() {
    // Test values near boundaries
    assertEquals(Direction.UP, Direction.fromAngle(20)); // Close to UP
    assertEquals(Direction.UP_RIGHT, Direction.fromAngle(50)); // Close to UP_RIGHT
    assertEquals(Direction.RIGHT, Direction.fromAngle(85)); // Close to RIGHT
    assertEquals(Direction.DOWN_RIGHT, Direction.fromAngle(140)); // Close to DOWN_RIGHT
  }

  @Test
  void fromAngle_shouldHandleBoundaryValues() {
    // Test exact midpoints between directions (Math.round rounds 0.5 up)
    assertEquals(Direction.UP_RIGHT, Direction.fromAngle(22.5f)); // Midpoint between UP and UP_RIGHT
    assertEquals(Direction.RIGHT, Direction.fromAngle(67.5f)); // Midpoint between UP_RIGHT and RIGHT
  }

  @Test
  void values_shouldContainEightDirections() {
    Direction[] directions = Direction.values();
    assertEquals(8, directions.length);
  }

  @Test
  void valueOf_shouldReturnCorrectDirection() {
    assertEquals(Direction.UP, Direction.valueOf("UP"));
    assertEquals(Direction.DOWN, Direction.valueOf("DOWN"));
    assertEquals(Direction.LEFT, Direction.valueOf("LEFT"));
    assertEquals(Direction.RIGHT, Direction.valueOf("RIGHT"));
  }
}
