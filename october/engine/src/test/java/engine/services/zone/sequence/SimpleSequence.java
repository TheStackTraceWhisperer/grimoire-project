package engine.services.zone.sequence;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * Simple implementation of Sequence for testing purposes.
 */
@Getter
@Builder
public class SimpleSequence implements Sequence {
  private final String id;
  private final List<GameEvent> events;
}
