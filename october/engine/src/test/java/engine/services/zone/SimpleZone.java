package engine.services.zone;

import engine.services.zone.sequence.Sequence;
import engine.services.zone.sequence.Trigger;
import engine.services.zone.tilemap.Tilemap;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 * Simple implementation of Zone for testing purposes.
 */
@Getter
@Builder
public class SimpleZone implements Zone {
  private final String id;
  private final String name;
  private final Tilemap tilemap;
  private final List<Sequence> sequences;
  private final List<Trigger> triggers;
  private final Map<String, Object> properties;
}
