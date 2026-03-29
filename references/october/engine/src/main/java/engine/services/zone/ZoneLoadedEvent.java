package engine.services.zone;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Event emitted when a zone has been successfully loaded.
 */
@Getter
@RequiredArgsConstructor
public class ZoneLoadedEvent {
  private final Zone zone;
}
