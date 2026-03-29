package engine.services.zone.sequence;

import java.util.List;
import java.util.Map;

/**
 * A data structure that defines a condition and the events that fire when the condition is met.
 */
public interface Trigger {
  /**
   * @return A unique identifier for the trigger.
   */
  String getId();

  /**
   * @return The condition type (e.g., "ON_LOAD", "ON_ENTER_AREA", "ON_INTERACT").
   */
  String getType();

  /**
   * @return The list of events to execute when triggered.
   * This is often just a single START_SEQUENCE event.
   */
  List<GameEvent> getEvents();

  /**
   * @return Parameters for the condition (e.g., "delay": 5.0, "bounds": {...}).
   */
  Map<String, Object> getProperties();
}
