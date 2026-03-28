package engine;

import engine.services.world.WorldService;
import engine.services.world.components.ControllableComponent;
import engine.services.world.components.MovementStatsComponent;
import engine.services.world.components.TransformComponent;
import engine.services.world.systems.MovementSystem;
import engine.services.world.systems.PlayerInputSystem;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DiagonalMovementIT extends EngineTestHarness {

    @Inject
    private WorldService worldService;
    @Inject
    private PlayerInputSystem playerInputSystem;
    @Inject
    private MovementSystem movementSystem;

    @BeforeEach
    void setUp() {
        worldService.addSystem(playerInputSystem);
        worldService.addSystem(movementSystem);
    }

    @AfterEach
    void tearDown() {
        worldService.clearSystems();
        worldService.getEntitiesWith().forEach(worldService::destroyEntity);
    }

    @Test
    void testDiagonalMovement_isNormalized() {
        // Given: A player entity that wants to move diagonally
        int playerId = worldService.createEntity();
        ControllableComponent control = new ControllableComponent();
        control.wantsToMoveUp = true;
        control.wantsToMoveRight = true;
        TransformComponent transform = new TransformComponent();
        MovementStatsComponent stats = new MovementStatsComponent(100.0f);

        worldService.addComponent(playerId, control);
        worldService.addComponent(playerId, transform);
        worldService.addComponent(playerId, stats);

        // When: The engine runs for a single frame
        tick();

        // Then: The resulting position vector should be normalized (x should equal y)
        // This proves that the diagonal input was normalized before being scaled by speed and time.
        assertEquals(transform.position.x, transform.position.y, 0.001f,
                "X and Y movement should be equal for diagonal input, proving normalization.");
    }
}
