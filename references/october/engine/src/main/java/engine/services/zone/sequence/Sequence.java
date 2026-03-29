package engine.services.zone.sequence;

import java.util.List;

/**
 * An ordered list of GameEvents that form a single script.
 */
public interface Sequence {
  /**
   * @return The unique identifier for this sequence (e.g., "intro_cutscene").
   */
  String getId();

  /**
   * @return The ordered list of commands to execute.
   */
  List<GameEvent> getEvents();
}
