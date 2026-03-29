package engine.services.world.systems;

import engine.services.rendering.FadeService;
import engine.services.resources.AssetCacheService;
import engine.services.audio.AudioService;
import engine.services.state.ApplicationStateService;
import engine.services.world.WorldService;
import engine.services.world.components.ActiveSequenceComponent;
import engine.services.world.components.SoundEffectComponent;
import engine.services.world.components.TransformComponent;
import engine.services.zone.Zone;
import engine.services.zone.ZoneService;
import engine.services.zone.sequence.GameEvent;
import engine.services.zone.sequence.Sequence;
import io.micronaut.context.BeanProvider;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
public class SequenceSystemIT {

  @Inject
  WorldService worldService;

  @AfterEach
  void cleanup() {
    worldService.clearSystems();
    worldService.getEntitiesWith().forEach(worldService::destroyEntity);
  }

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
  static class TestZoneService extends ZoneService {
    private final Zone zone;
    public TestZoneService(Zone z) {
      super(new engine.services.event.EventPublisherService(new ApplicationEventPublisher() {
        @Override public void publishEvent(Object event) { /* no-op */ }
      }));
      this.zone = z;
    }
    @Override public Zone getCurrentZone() { return zone; }
  }

  private SequenceSystem makeSystem(Zone zone, FadeService fade) {
    // Use a real AudioSystem instance (it won't touch OpenAL unless started); playSoundEffect only adds a component
    var audioSystem = new AudioSystem(new AudioService(), new AssetCacheService());
    var eventPublisher = new engine.services.event.EventPublisherService(new ApplicationEventPublisher() {
      @Override public void publishEvent(Object event) { /* no-op */ }
    });
    @SuppressWarnings("unchecked")
    BeanProvider<ApplicationStateService> stateServiceProvider = Mockito.mock(BeanProvider.class);
    ApplicationContext appContext = Mockito.mock(ApplicationContext.class);
    // Keep ActiveSequenceComponent present after completion so ITs can assert state
    return new SequenceSystem(new TestZoneService(zone), audioSystem, fade, eventPublisher, stateServiceProvider, appContext, false);
  }

  @Test
  void playSound_addsSoundEffectComponent_and_advances() {
    var ev = new TestEvent("PLAY_SOUND", Map.of("soundId", "sfx_click", "volume", 0.5));
    var seq = new TestSequence("seq1", List.of(ev));
    var zone = new TestZone("zone1", List.of(seq));

    var fade = new FadeService();
    var sequenceSystem = makeSystem(zone, fade);

    int e = worldService.createEntity();
    worldService.addComponent(e, new ActiveSequenceComponent("seq1"));

    worldService.addSystem(sequenceSystem);

    worldService.update(0.016f);

    var active = worldService.getComponent(e, ActiveSequenceComponent.class);
    assertNotNull(active);
    assertEquals(1, active.getCurrentIndex());

    // A transient entity should have been created with a SoundEffectComponent
    var sfxEntities = worldService.getEntitiesWith(SoundEffectComponent.class);
    assertFalse(sfxEntities.isEmpty());
  }

  @Test
  void teleportEntity_updatesTransform_and_advances() {
    var ev = new TestEvent("TELEPORT_ENTITY", Map.of("entityId", "self", "x", 3.0, "y", 4.0, "z", 0.0));
    var seq = new TestSequence("seq2", List.of(ev));
    var zone = new TestZone("zone2", List.of(seq));

    var sequenceSystem = makeSystem(zone, new FadeService());

    int e = worldService.createEntity();
    worldService.addComponent(e, new TransformComponent());
    worldService.addComponent(e, new ActiveSequenceComponent("seq2"));

    worldService.addSystem(sequenceSystem);
    worldService.update(0.016f);

    var t = worldService.getComponent(e, TransformComponent.class);
    assertEquals(3.0f, t.position.x, 1e-5);
    assertEquals(4.0f, t.position.y, 1e-5);

    var active = worldService.getComponent(e, ActiveSequenceComponent.class);
    assertEquals(1, active.getCurrentIndex());
  }

  @Test
  void moveEntity_blocks_until_arrival_then_advances() {
    var ev = new TestEvent("MOVE_ENTITY", Map.of("x", 2.0, "y", 0.0, "z", 0.0, "speed", 10.0, "tolerance", 0.001));
    var seq = new TestSequence("seq3", List.of(ev));
    var zone = new TestZone("zone3", List.of(seq));

    var sequenceSystem = makeSystem(zone, new FadeService());
    var moveSystem = new MoveToTargetSystem();

    int e = worldService.createEntity();
    worldService.addComponent(e, new TransformComponent());
    worldService.addComponent(e, new ActiveSequenceComponent("seq3"));

    worldService.addSystem(sequenceSystem);
    worldService.addSystem(moveSystem);

    worldService.update(0.016f);
    var active = worldService.getComponent(e, ActiveSequenceComponent.class);
    assertTrue(active.isBlocked());

    for (int i = 0; i < 10; i++) {
      worldService.update(0.016f);
    }

    active = worldService.getComponent(e, ActiveSequenceComponent.class);
    assertFalse(active.isBlocked());
    assertEquals(1, active.getCurrentIndex());

    var t = worldService.getComponent(e, TransformComponent.class);
    assertEquals(2.0f, t.position.x, 1e-3);
  }

  @Test
  void fadeScreen_blocks_then_advances_after_duration() {
    var ev = new TestEvent("FADE_SCREEN", Map.of("fadeType", "OUT", "duration", 0.05));
    var seq = new TestSequence("seq4", List.of(ev));
    var zone = new TestZone("zone4", List.of(seq));

    var fade = new FadeService();
    var sequenceSystem = makeSystem(zone, fade);

    int e = worldService.createEntity();
    worldService.addComponent(e, new ActiveSequenceComponent("seq4"));

    worldService.addSystem(sequenceSystem);

    worldService.update(0.016f);
    var active = worldService.getComponent(e, ActiveSequenceComponent.class);
    assertTrue(active.isBlocked());
    assertTrue(fade.isFading());

    for (int i = 0; i < 10; i++) {
      fade.update(0.01f);
      worldService.update(0.01f);
    }

    active = worldService.getComponent(e, ActiveSequenceComponent.class);
    assertFalse(active.isBlocked());
    assertEquals(1, active.getCurrentIndex());
  }
}
