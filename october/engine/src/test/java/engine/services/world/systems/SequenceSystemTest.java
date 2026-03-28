package engine.services.world.systems;

import engine.services.event.EventPublisherService;
import engine.services.state.ApplicationStateService;
import engine.services.world.World;
import engine.services.world.components.ActiveSequenceComponent;
import engine.services.zone.Zone;
import engine.services.zone.ZoneService;
import engine.services.zone.sequence.GameEvent;
import engine.services.zone.sequence.Sequence;
import engine.services.rendering.FadeService;
import io.micronaut.context.BeanProvider;
import io.micronaut.context.ApplicationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SequenceSystemTest {

  @Mock
  private ZoneService zoneService;

  @Mock
  private AudioSystem audioSystem;

  @Mock
  private FadeService fadeService;

  @Mock
  private EventPublisherService eventPublisherService;

  @Mock
  private BeanProvider<ApplicationStateService> applicationStateServiceProvider;

  @Mock
  private ApplicationContext applicationContext;

  private World world;
  private SequenceSystem sequenceSystem;

  // --- Test helpers ---
  static class TestEvent implements GameEvent {
    private final String type; private final Map<String, Object> props;
    TestEvent(String type, Map<String, Object> props) { this.type = type; this.props = props; }
    @Override public String getType() { return type; }
    @Override public Map<String, Object> getProperties() { return props; }
  }
  static class TestSequence implements Sequence {
    private final String id; private final List<GameEvent> events;
    TestSequence(String id, List<GameEvent> events) { this.id = id; this.events = events; }
    @Override public String getId() { return id; }
    @Override public List<GameEvent> getEvents() { return events; }
  }
  static class TestZone implements Zone {
    private final String id; private final List<Sequence> seqs;
    TestZone(String id, List<Sequence> seqs) { this.id = id; this.seqs = seqs; }
    @Override public String getId() { return id; }
    @Override public String getName() { return id; }
    @Override public engine.services.zone.tilemap.Tilemap getTilemap() { return null; }
    @Override public List<Sequence> getSequences() { return seqs; }
    @Override public List<engine.services.zone.sequence.Trigger> getTriggers() { return Collections.emptyList(); }
    @Override public Map<String, Object> getProperties() { return Collections.emptyMap(); }
  }

  @BeforeEach
  void setUp() {
    world = new World();
    sequenceSystem = new SequenceSystem(zoneService, audioSystem, fadeService, eventPublisherService, applicationStateServiceProvider, applicationContext);
  }

  @Test
  void testWaitEventDecreasesTimer() {
    // Given a sequence with a WAIT event
    GameEvent waitEvent = new TestEvent("WAIT", Map.of("duration", 2.0));
    Sequence sequence = new TestSequence("wait_sequence", List.of(waitEvent));
    Zone zone = new TestZone("test_zone", List.of(sequence));

    when(zoneService.getCurrentZone()).thenReturn(zone);

    // When we create an entity with ActiveSequenceComponent
    int entityId = world.createEntity();
    ActiveSequenceComponent component = new ActiveSequenceComponent("wait_sequence");
    world.addComponent(entityId, component);

    // And update the sequence system once to process the WAIT event
    sequenceSystem.update(world, 0.01f);

    // Then the wait timer should be set
    assertThat(component.getWaitTimer()).isGreaterThan(1.9f);
    assertThat(component.getCurrentIndex()).isEqualTo(0);

    // When we update with 1.5 seconds
    sequenceSystem.update(world, 1.5f);

    // Then the timer should decrease
    assertThat(component.getWaitTimer()).isLessThan(1.0f);
    assertThat(component.getCurrentIndex()).isEqualTo(0);

    // When we update with another 1 second
    sequenceSystem.update(world, 1.0f);

    // Then the timer should reach 0 and index should advance
    assertThat(component.getWaitTimer()).isEqualTo(0.0f);
    assertThat(component.getCurrentIndex()).isEqualTo(1);
  }

  @Test
  void testInstantActionAdvancesIndex() {
    // Given a sequence with a PLAY_SOUND event
    GameEvent playSoundEvent = new TestEvent("PLAY_SOUND", Map.of("soundId", "test_sound"));
    Sequence sequence = new TestSequence("sound_sequence", List.of(playSoundEvent));
    Zone zone = new TestZone("test_zone", List.of(sequence));

    when(zoneService.getCurrentZone()).thenReturn(zone);

    // When we create an entity with ActiveSequenceComponent
    int entityId = world.createEntity();
    ActiveSequenceComponent component = new ActiveSequenceComponent("sound_sequence");
    world.addComponent(entityId, component);

    // And update the sequence system
    sequenceSystem.update(world, 0.01f);

    // Then the index should advance immediately
    assertThat(component.getCurrentIndex()).isEqualTo(1);
  }

  @Test
  void testSequenceCompletionRemovesComponent() {
    // Given a sequence with a single instant event
    GameEvent playSoundEvent = new TestEvent("PLAY_SOUND", Map.of("soundId", "test_sound"));
    Sequence sequence = new TestSequence("complete_sequence", List.of(playSoundEvent));
    Zone zone = new TestZone("test_zone", List.of(sequence));

    when(zoneService.getCurrentZone()).thenReturn(zone);

    // When we create an entity with ActiveSequenceComponent
    int entityId = world.createEntity();
    world.addComponent(entityId, new ActiveSequenceComponent("complete_sequence"));

    // And update the sequence system to execute the event
    sequenceSystem.update(world, 0.01f);

    // Then the component should advance to index 1
    var component = world.getComponent(entityId, ActiveSequenceComponent.class);
    assertThat(component.getCurrentIndex()).isEqualTo(1);

    // When we update again
    sequenceSystem.update(world, 0.01f);

    // Then the entity should be destroyed (sequence complete)
    var entities = world.getEntitiesWith(ActiveSequenceComponent.class);
    assertThat(entities).isEmpty();
  }

  @Test
  void testBlockedSequenceDoesNotAdvance() {
    // Given a sequence with a MOVE_ENTITY event
    GameEvent moveEvent = new TestEvent("MOVE_ENTITY", Map.of("entityId", "player"));
    Sequence sequence = new TestSequence("move_sequence", List.of(moveEvent));
    Zone zone = new TestZone("test_zone", List.of(sequence));

    when(zoneService.getCurrentZone()).thenReturn(zone);

    // When we create an entity with ActiveSequenceComponent
    int entityId = world.createEntity();
    ActiveSequenceComponent component = new ActiveSequenceComponent("move_sequence");
    world.addComponent(entityId, component);

    // And update the sequence system
    sequenceSystem.update(world, 0.01f);

    // Then the component should be blocked
    assertThat(component.isBlocked()).isTrue();
    assertThat(component.getCurrentIndex()).isEqualTo(0);

    // When we update again while still blocked
    sequenceSystem.update(world, 0.01f);

    // Then the index should not advance
    assertThat(component.getCurrentIndex()).isEqualTo(0);
  }

  @Test
  void testUpdateWithNoZoneDoesNothing() {
    // Given no zone is loaded
    when(zoneService.getCurrentZone()).thenReturn(null);

    // When we create an entity with ActiveSequenceComponent
    int entityId = world.createEntity();
    world.addComponent(entityId, new ActiveSequenceComponent("test_sequence"));

    // And update the sequence system
    sequenceSystem.update(world, 0.01f);

    // Then the entity should still exist (nothing happened)
    var entities = world.getEntitiesWith(ActiveSequenceComponent.class);
    assertThat(entities).hasSize(1);
  }

  @Test
  void testUnknownEventTypeAdvancesIndex() {
    // Given a sequence with an unknown event type
    GameEvent unknownEvent = new TestEvent("UNKNOWN_TYPE", new HashMap<>());
    Sequence sequence = new TestSequence("unknown_sequence", List.of(unknownEvent));
    Zone zone = new TestZone("test_zone", List.of(sequence));

    when(zoneService.getCurrentZone()).thenReturn(zone);

    // When we create an entity with ActiveSequenceComponent
    int entityId = world.createEntity();
    ActiveSequenceComponent component = new ActiveSequenceComponent("unknown_sequence");
    world.addComponent(entityId, component);

    // And update the sequence system
    sequenceSystem.update(world, 0.01f);

    // Then the index should advance (to avoid getting stuck)
    assertThat(component.getCurrentIndex()).isEqualTo(1);
  }
}
