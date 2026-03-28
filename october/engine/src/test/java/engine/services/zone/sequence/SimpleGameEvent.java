package engine.services.zone.sequence;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

/**
 * Simple implementation of GameEvent for testing purposes.
 */
@Getter
@Builder
public class SimpleGameEvent implements GameEvent {
  private final String type;
  private final Map<String, Object> properties;
}
