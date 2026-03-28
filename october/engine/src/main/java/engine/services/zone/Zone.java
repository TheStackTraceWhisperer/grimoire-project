package engine.services.zone;

import engine.services.zone.sequence.Sequence;
import engine.services.zone.sequence.Trigger;
import engine.services.zone.tilemap.Tilemap;

import java.util.List;
import java.util.Map;

/** Zone definition: layout, sequences, triggers, and properties. */
public interface Zone {
  /** Zone id (e.g., "forest_glade_01"). */
  String getId();

  /** Display name. */
  String getName();

  /** Tilemap for this zone. */
  Tilemap getTilemap();

  /** All sequences. */
  List<Sequence> getSequences();

  /** All triggers. */
  List<Trigger> getTriggers();

  /** Arbitrary metadata. */
  Map<String, Object> getProperties();
}
