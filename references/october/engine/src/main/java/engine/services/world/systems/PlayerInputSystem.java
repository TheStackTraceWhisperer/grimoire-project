package engine.services.world.systems;

import engine.game.GameAction;
import engine.services.input.DeviceMappingService;
import engine.services.world.ISystem;
import engine.services.world.World;
import engine.services.world.components.ControllableComponent;
import io.micronaut.context.annotation.Prototype;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;

/**
 * This system reads from the DeviceMappingService and updates the state of all
 * ControllableComponent instances based on the player's 2D input.
 */
@Prototype
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class PlayerInputSystem implements ISystem {

  private final DeviceMappingService mappingService;

  @Override
  public void update(World world, float deltaTime) {
    for (int entityId : world.getEntitiesWith(ControllableComponent.class)) {
      ControllableComponent control = world.getComponent(entityId, ControllableComponent.class);
      int playerId = control.playerId;

      // Update the component's state based on the abstract 2D actions
      control.wantsToMoveUp = mappingService.isActionActive(playerId, GameAction.MOVE_UP);
      control.wantsToMoveDown = mappingService.isActionActive(playerId, GameAction.MOVE_DOWN);
      control.wantsToMoveLeft = mappingService.isActionActive(playerId, GameAction.MOVE_LEFT);
      control.wantsToMoveRight = mappingService.isActionActive(playerId, GameAction.MOVE_RIGHT);
      control.wantsToAttack = mappingService.isActionActive(playerId, GameAction.ATTACK);
    }
  }
}
