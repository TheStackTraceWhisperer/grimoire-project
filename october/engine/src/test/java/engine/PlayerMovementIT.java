package engine;

import engine.game.GameColliderType;
import engine.services.world.WorldService;
import engine.services.world.components.ColliderComponent;
import engine.services.world.components.ControllableComponent;
import engine.services.world.components.MovementStatsComponent;
import engine.services.world.components.TransformComponent;
import engine.services.world.systems.CollisionSystem;
import engine.services.world.systems.MovementSystem;
import engine.services.world.systems.PlayerInputSystem;
import jakarta.inject.Inject;
import org.joml.Vector3f;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class PlayerMovementIT extends EngineTestHarness {

    @Inject
    private WorldService worldService;
    @Inject
    private PlayerInputSystem playerInputSystem;
    @Inject
    private MovementSystem movementSystem;
    @Inject
    private CollisionSystem collisionSystem;

    @BeforeEach
    void setUp() {
        worldService.addSystem(playerInputSystem);
        worldService.addSystem(movementSystem);
        worldService.addSystem(collisionSystem);
    }

    @AfterEach
    void tearDown() {
        worldService.clearSystems();
        worldService.getEntitiesWith().forEach(worldService::destroyEntity);
    }

    @Test
    void testPlayerCollidesWithWall() {
        // Given: A player entity and a wall entity to its right
        int player = createPlayer(new Vector3f(0, 0, 0));
        int wall = createWall(new Vector3f(1, 0, 0));

        // And: The player wants to move right
        ControllableComponent control = worldService.getComponent(player, ControllableComponent.class);
        control.wantsToMoveRight = true;

        // When: We run a single frame
        tick();

        // Then: The player's position should have been reverted by the collision system
        TransformComponent playerTransform = worldService.getComponent(player, TransformComponent.class);
        assertTrue(playerTransform.position.x <= 0.0f, "Player should not have moved past the wall.");
    }

    private int createPlayer(Vector3f position) {
        int entity = worldService.createEntity();
        TransformComponent transform = new TransformComponent();
        transform.position.set(position);
        worldService.addComponent(entity, transform);
        worldService.addComponent(entity, new ControllableComponent());
        worldService.addComponent(entity, new MovementStatsComponent(5.0f));
        worldService.addComponent(entity, new ColliderComponent(GameColliderType.PLAYER, 1, 1, 0, 0));
        return entity;
    }

    private int createWall(Vector3f position) {
        int entity = worldService.createEntity();
        TransformComponent transform = new TransformComponent();
        transform.position.set(position);
        worldService.addComponent(entity, transform);
        worldService.addComponent(entity, new ColliderComponent(GameColliderType.WALL, 1, 1, 0, 0));
        return entity;
    }
}
