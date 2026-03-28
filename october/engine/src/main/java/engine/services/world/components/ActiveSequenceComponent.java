package engine.services.world.components;

import engine.services.world.IComponent;
import io.micronaut.core.annotation.Introspected;
import lombok.Getter;
import lombok.Setter;

/**
 * A stateful "bookmark" component added to an entity to signify that a sequence is currently running.
 * The SequenceSystem acts upon entities that have this component.
 */
@Introspected
@Getter
@Setter
public class ActiveSequenceComponent implements IComponent {
  /**
   * The ID of the Sequence (from the current Zone) to execute.
   */
  private String sequenceId;

  /**
   * The index of the current GameEvent being processed.
   */
  private int currentIndex;

  /**
   * A countdown timer used for the "WAIT" command.
   */
  private float waitTimer;

  /**
   * A flag set to true when the sequence is waiting for another system
   * (e.g., MovementSystem) to complete a long-running task.
   */
  private boolean isBlocked;

  // Track the condition we're waiting on (e.g., MOVE_ENTITY for a specific entity)
  private String waitForAction; // e.g., "MOVE_ENTITY"
  private Integer waitForEntityId; // entity id that must finish the action

  public ActiveSequenceComponent(String sequenceId) {
    this.sequenceId = sequenceId;
    this.currentIndex = 0;
    this.waitTimer = 0.0f;
    this.isBlocked = false;
    this.waitForAction = null;
    this.waitForEntityId = null;
  }
}
