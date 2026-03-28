package engine.services.zone.sequence;

import java.util.Map;

/**
 * A single, atomic command within a sequence.
 */
public interface GameEvent {
  /**
   * @return The command type (e.g., "MOVE_ENTITY", "WAIT").
   */
  String getType();

  /**
   * @return The parameters for the command (e.g., "entityId": "player", "duration": 5.0).
   */
  Map<String, Object> getProperties();
}
