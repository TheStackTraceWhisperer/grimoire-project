package engine.services.world.systems;

import engine.services.world.World;
import engine.services.world.components.ActiveSequenceComponent;
import engine.services.zone.Zone;
import engine.services.zone.ZoneLoadedEvent;
import engine.services.zone.ZoneService;
import engine.services.zone.sequence.GameEvent;
import engine.services.zone.sequence.Sequence;
import engine.services.zone.sequence.Trigger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TriggerSystemTest {

  @Mock
  private ZoneService zoneService;

  private World world;
  private TriggerSystem triggerSystem;

  static class TestEvent implements GameEvent {
    private final String type; private final Map<String, Object> props;
    TestEvent(String type, Map<String, Object> props) { this.type = type; this.props = props; }
    @Override public String getType() { return type; }
    @Override public Map<String, Object> getProperties() { return props; }
  }
  static class TestTrigger implements Trigger {
    private final String id; private final String type; private final List<GameEvent> events; private final Map<String, Object> props;
    TestTrigger(String id, String type, List<GameEvent> events, Map<String, Object> props) {
      this.id = id; this.type = type; this.events = events; this.props = props; }
    @Override public String getId() { return id; }
    @Override public String getType() { return type; }
    @Override public List<GameEvent> getEvents() { return events; }
    @Override public Map<String, Object> getProperties() { return props; }
  }
  static class TestZone implements Zone {
    private final String id; private final List<Sequence> seqs; private final List<Trigger> triggers;
    TestZone(String id, List<Sequence> seqs, List<Trigger> triggers) { this.id = id; this.seqs = seqs; this.triggers = triggers; }
    @Override public String getId() { return id; }
    @Override public String getName() { return id; }
    @Override public engine.services.zone.tilemap.Tilemap getTilemap() { return null; }
    @Override public List<Sequence> getSequences() { return seqs; }
    @Override public List<Trigger> getTriggers() { return triggers; }
    @Override public Map<String, Object> getProperties() { return Map.of(); }
  }

  @BeforeEach
  void setUp() {
    world = new World();
    triggerSystem = new TriggerSystem(zoneService);
  }

  @Test
  void onLoad_trigger_fires_after_delay_and_starts_sequence() {
    // Given a zone with an ON_LOAD trigger that starts a sequence
    GameEvent startSeq = new TestEvent("START_SEQUENCE", Map.of("sequenceId", "seqA"));
    Trigger trigger = new TestTrigger("t1", "ON_LOAD", List.of(startSeq), Map.of("delay", 0.05));
    Zone zone = new TestZone("z1", List.of(), List.of(trigger));

    when(zoneService.getCurrentZone()).thenReturn(zone);

    // Simulate zone loaded
    triggerSystem.onApplicationEvent(new ZoneLoadedEvent(zone));

    // Initially, not fired (before delay)
    triggerSystem.update(world, 0.01f);
    assertThat(world.getEntitiesWith(ActiveSequenceComponent.class)).isEmpty();

    // After reaching delay, it should fire
    triggerSystem.update(world, 0.05f);
    assertThat(world.getEntitiesWith(ActiveSequenceComponent.class)).hasSize(1);
  }
}
