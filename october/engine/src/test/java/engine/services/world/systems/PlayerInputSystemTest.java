package engine.services.world.systems;

import engine.game.GameAction;
import engine.services.input.DeviceMappingService;
import engine.services.world.World;
import engine.services.world.components.ControllableComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlayerInputSystemTest {

    @Mock
    private DeviceMappingService mappingService;

    @Mock
    private World world;

    @InjectMocks
    private PlayerInputSystem playerInputSystem;

    private ControllableComponent control;
    private final int entityId = 1;
    private final int playerId = 0;

    @BeforeEach
    void setUp() {
        control = new ControllableComponent();
        control.playerId = playerId;

        // Configure the mock world to return our test entity
        lenient().when(world.getEntitiesWith(ControllableComponent.class)).thenReturn(Set.of(entityId));
        lenient().when(world.getComponent(entityId, ControllableComponent.class)).thenReturn(control);

        // Default: no actions active for any player/action
        lenient().when(mappingService.isActionActive(anyInt(), any(GameAction.class))).thenReturn(false);
    }

    @Test
    void testSystem_updatesComponentStateFromDeviceMappings() {
        // Given: The mapping service reports that MOVE_UP and ATTACK are active
        when(mappingService.isActionActive(playerId, GameAction.MOVE_UP)).thenReturn(true);
        when(mappingService.isActionActive(playerId, GameAction.ATTACK)).thenReturn(true);
        // All other actions will return false from the default stub

        // When: The system updates
        playerInputSystem.update(world, 0.1f);

        // Then: The ControllableComponent's state should be updated accordingly
        assertTrue(control.wantsToMoveUp, "wantsToMoveUp should be true.");
        assertTrue(control.wantsToAttack, "wantsToAttack should be true.");
        assertFalse(control.wantsToMoveDown, "wantsToMoveDown should remain false.");
        assertFalse(control.wantsToMoveLeft, "wantsToMoveLeft should remain false.");
        assertFalse(control.wantsToMoveRight, "wantsToMoveRight should remain false.");
    }

    @Test
    void testSystem_handlesNoActiveActions() {
        // Given: The mapping service reports no active actions (default mock behavior)
        // All isActionActive calls will return false

        // When: The system updates
        playerInputSystem.update(world, 0.1f);

        // Then: All control flags should be false
        assertFalse(control.wantsToMoveUp);
        assertFalse(control.wantsToMoveDown);
        assertFalse(control.wantsToMoveLeft);
        assertFalse(control.wantsToMoveRight);
        assertFalse(control.wantsToAttack);
    }

    @Test
    void testSystem_handlesAllDirectionalInputsSimultaneously() {
        // Given: All directional inputs are active
        when(mappingService.isActionActive(playerId, GameAction.MOVE_UP)).thenReturn(true);
        when(mappingService.isActionActive(playerId, GameAction.MOVE_DOWN)).thenReturn(true);
        when(mappingService.isActionActive(playerId, GameAction.MOVE_LEFT)).thenReturn(true);
        when(mappingService.isActionActive(playerId, GameAction.MOVE_RIGHT)).thenReturn(true);

        // When: The system updates
        playerInputSystem.update(world, 0.1f);

        // Then: All directional flags should be set
        assertTrue(control.wantsToMoveUp);
        assertTrue(control.wantsToMoveDown);
        assertTrue(control.wantsToMoveLeft);
        assertTrue(control.wantsToMoveRight);
    }

    @Test
    void testSystem_handlesNoEntities() {
        // Given: No entities have ControllableComponent
        when(world.getEntitiesWith(ControllableComponent.class)).thenReturn(Set.of());

        // When: The system updates
        // Then: No exception should be thrown
        assertDoesNotThrow(() -> playerInputSystem.update(world, 0.1f));
    }

    @Test
    void testSystem_handlesMultipleEntities() {
        // Given: Multiple entities with different player IDs
        int entityId2 = 2;
        int playerId2 = 1;
        ControllableComponent control2 = new ControllableComponent();
        control2.playerId = playerId2;

        when(world.getEntitiesWith(ControllableComponent.class)).thenReturn(Set.of(entityId, entityId2));
        when(world.getComponent(entityId2, ControllableComponent.class)).thenReturn(control2);

        // Set different actions for different players
        when(mappingService.isActionActive(playerId, GameAction.MOVE_UP)).thenReturn(true);
        when(mappingService.isActionActive(playerId2, GameAction.MOVE_DOWN)).thenReturn(true);

        // When: The system updates
        playerInputSystem.update(world, 0.1f);

        // Then: Each entity should reflect its player's input
        assertTrue(control.wantsToMoveUp);
        assertFalse(control.wantsToMoveDown);
        assertFalse(control2.wantsToMoveUp);
        assertTrue(control2.wantsToMoveDown);
    }
}
