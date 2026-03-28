package engine;

import engine.game.EnemyComponent;
import engine.services.time.SystemTimeService;
import engine.services.world.WorldService;
import engine.services.world.components.TransformComponent;
import engine.services.world.systems.EnemyAISystem;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EnemyAIMovementIT extends EngineTestHarness {

    @Inject
    private WorldService worldService;
    @Inject
    private SystemTimeService timeService;
    @Inject
    private EnemyAISystem enemyAISystem;

    @BeforeEach
    void setUp() {
        worldService.addSystem(enemyAISystem);
    }

    @AfterEach
    void tearDown() {
        worldService.clearSystems();
        worldService.getEntitiesWith().forEach(worldService::destroyEntity);
    }

    @Test
    void testEnemyPatrolMovement() {
        // Given: An enemy entity
        int enemyId = worldService.createEntity();
        TransformComponent transform = new TransformComponent();
        worldService.addComponent(enemyId, new EnemyComponent());
        worldService.addComponent(enemyId, transform);

        // When: We run the engine for a few frames
        tick();
        tick();
        tick();

        // Then: The enemy's X position should match the expected position from the sine wave calculation
        float travelDistance = 3.0f; // This value is from EnemyAISystem
        float expectedX = (float) Math.sin(timeService.getTotalTimeSeconds()) * travelDistance;

        assertEquals(expectedX, transform.position.x, 0.001f,
                "Enemy X position should be updated by the EnemyAISystem based on total game time.");
    }
}
