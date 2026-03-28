package engine.services.world.systems;

import engine.services.world.ISystem;
import engine.services.world.World;
import engine.services.world.components.ActiveSequenceComponent;
import engine.services.zone.Zone;
import engine.services.zone.ZoneLoadedEvent;
import engine.services.zone.ZoneService;
import engine.services.zone.sequence.GameEvent;
import engine.services.zone.sequence.Trigger;
import io.micronaut.context.annotation.Prototype;
import io.micronaut.context.event.ApplicationEventListener;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * A stateless system that observes game state and fires events from Trigger data.
 */
@Slf4j
@Prototype
public class TriggerSystem implements ISystem, ApplicationEventListener<ZoneLoadedEvent> {
  private final ZoneService zoneService;

  // Track which triggers have already fired (for non-repeatable triggers)
  private final Map<String, Boolean> firedTriggers = new HashMap<>();

  // Track time since zone loaded (for ON_LOAD triggers with delay)
  private float timeSinceZoneLoaded = 0.0f;
  private boolean zoneJustLoaded = false;

  public TriggerSystem(ZoneService zoneService) {
    this.zoneService = zoneService;
  }

  @Override
  public void onApplicationEvent(ZoneLoadedEvent event) {
    log.debug("Zone loaded, resetting trigger system");
    firedTriggers.clear();
    timeSinceZoneLoaded = 0.0f;
    zoneJustLoaded = true;
  }

  @Override
  public void update(World world, float deltaTime) {
    Zone currentZone = zoneService.getCurrentZone();
    if (currentZone == null) {
      return;
    }

    // Update time since zone loaded
    if (zoneJustLoaded || timeSinceZoneLoaded > 0) {
      timeSinceZoneLoaded += deltaTime;
    }

    // Iterate through all triggers in the current zone
    for (Trigger trigger : currentZone.getTriggers()) {
      // Check if this trigger has already fired and is not repeatable
      boolean isRepeatable = (Boolean) trigger.getProperties().getOrDefault("isRepeatable", false);
      if (firedTriggers.getOrDefault(trigger.getId(), false) && !isRepeatable) {
        continue;
      }

      // Check if the trigger's condition is met
      if (isTriggerConditionMet(trigger)) {
        log.debug("Trigger condition met: {}", trigger.getId());
        executeTrigger(world, trigger);
        firedTriggers.put(trigger.getId(), true);
      }
    }
  }

  /**
   * Checks if the condition for a trigger is met.
   */
  private boolean isTriggerConditionMet(Trigger trigger) {
    String type = trigger.getType();

    return switch (type) {
      case "ON_LOAD" -> {
        // Check if enough time has passed since zone load
        float delay = ((Number) trigger.getProperties().getOrDefault("delay", 0.0)).floatValue();
        yield timeSinceZoneLoaded >= delay;
      }
      case "ON_ENTER_AREA" -> {
        // TODO: Check if player entity is inside the bounds
        // This would require checking entity positions against trigger bounds
        log.debug("ON_ENTER_AREA trigger not yet fully implemented");
        yield false;
      }
      case "ON_INTERACT" -> {
        // TODO: Check if player is interacting with the trigger
        log.debug("ON_INTERACT trigger not yet fully implemented");
        yield false;
      }
      default -> {
        log.warn("Unknown trigger type: {}", type);
        yield false;
      }
    };
  }

  /**
   * Executes the events associated with a trigger.
   */
  private void executeTrigger(World world, Trigger trigger) {
    for (GameEvent event : trigger.getEvents()) {
      if ("START_SEQUENCE".equals(event.getType())) {
        String sequenceId = (String) event.getProperties().get("sequenceId");
        if (sequenceId != null) {
          log.debug("Starting sequence: {}", sequenceId);
          // Create a new entity and add an ActiveSequence component to it
          int entityId = world.createEntity();
          world.addComponent(entityId, new ActiveSequenceComponent(sequenceId));
        } else {
          log.warn("START_SEQUENCE event missing sequenceId property");
        }
      } else {
        log.warn("Unknown event type in trigger: {}", event.getType());
      }
    }
  }
}
