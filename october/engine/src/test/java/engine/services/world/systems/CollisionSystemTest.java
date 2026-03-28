package engine.services.world.systems;

import engine.game.GameColliderType;
import engine.services.world.World;
import engine.services.world.components.ColliderComponent;
import engine.services.world.components.TransformComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CollisionSystemTest {

    @Mock
    private World world;

    @InjectMocks
    private CollisionSystem collisionSystem;

    private TransformComponent playerTransform;
    private TransformComponent wallTransform;
    private ColliderComponent playerCollider;
    private ColliderComponent wallCollider;

    private final int playerEntity = 1;
    private final int wallEntity = 2;

    private void setupTwoEntityCollisionScenario() {
        // Use spy for transforms to track method calls on real objects
        playerTransform = spy(new TransformComponent());
        playerCollider = new ColliderComponent(GameColliderType.PLAYER, 1, 1, 0, 0);

        wallTransform = new TransformComponent();
        wallCollider = new ColliderComponent(GameColliderType.WALL, 1, 1, 0, 0);

        // Configure mock world to return our two entities
        when(world.getEntitiesWith(TransformComponent.class, ColliderComponent.class))
                .thenReturn(Set.of(playerEntity, wallEntity));

        when(world.getComponent(playerEntity, TransformComponent.class)).thenReturn(playerTransform);
        when(world.getComponent(playerEntity, ColliderComponent.class)).thenReturn(playerCollider);

        when(world.getComponent(wallEntity, TransformComponent.class)).thenReturn(wallTransform);
        when(world.getComponent(wallEntity, ColliderComponent.class)).thenReturn(wallCollider);
    }

    @Test
    void testCollision_revertsPositionWhenOverlapping() {
        // Given: Two entities setup
        setupTwoEntityCollisionScenario();

        // And: The player and wall are positioned to overlap
        playerTransform.position.set(0.5f, 0, 0);
        wallTransform.position.set(1.0f, 0, 0);

        // When: The collision system updates
        collisionSystem.update(world, 0.1f);

        // Then: The player's revertPosition method should have been called
        verify(playerTransform, times(1)).revertPosition();
    }

    @Test
    void testCollision_doesNothingWhenNotOverlapping() {
        // Given: Two entities setup
        setupTwoEntityCollisionScenario();

        // And: The player and wall are positioned far apart
        playerTransform.position.set(-5.0f, 0, 0);
        wallTransform.position.set(5.0f, 0, 0);

        // When: The collision system updates
        collisionSystem.update(world, 0.1f);

        // Then: The player's revertPosition method should NOT have been called
        verify(playerTransform, never()).revertPosition();
    }

    @Test
    void testCollision_handlesNoEntities() {
        // Given: No entities have the required components
        when(world.getEntitiesWith(TransformComponent.class, ColliderComponent.class))
                .thenReturn(Set.of());

        // When: The collision system updates
        // Then: No exception should be thrown
        collisionSystem.update(world, 0.1f);
    }

    @Test
    void testCollision_handlesSingleEntity() {
        // Given: Only one entity exists
        playerTransform = spy(new TransformComponent());
        playerCollider = new ColliderComponent(GameColliderType.PLAYER, 1, 1, 0, 0);

        when(world.getEntitiesWith(TransformComponent.class, ColliderComponent.class))
                .thenReturn(Set.of(playerEntity));
        lenient().when(world.getComponent(playerEntity, TransformComponent.class)).thenReturn(playerTransform);
        lenient().when(world.getComponent(playerEntity, ColliderComponent.class)).thenReturn(playerCollider);

        playerTransform.position.set(0, 0, 0);

        // When: The collision system updates
        collisionSystem.update(world, 0.1f);

        // Then: No collision should occur (can't collide with self)
        verify(playerTransform, never()).revertPosition();
    }

    @Test
    void testCollision_handlesEdgeTouching() {
        // Given: Two entities setup
        setupTwoEntityCollisionScenario();

        // And: Entities are positioned at exact edge boundaries
        playerTransform.position.set(0.0f, 0, 0);
        wallTransform.position.set(1.0f, 0, 0);

        // When: The collision system updates
        collisionSystem.update(world, 0.1f);

        // Then: Depends on collision detection implementation
        // This test documents behavior at boundaries
    }

    @Test
    void testCollision_handlesVerticalOverlap() {
        // Given: Two entities setup
        setupTwoEntityCollisionScenario();

        // And: Entities overlapping vertically
        playerTransform.position.set(0, 0.5f, 0);
        wallTransform.position.set(0, 1.0f, 0);

        // When: The collision system updates
        collisionSystem.update(world, 0.1f);

        // Then: Collision should be detected
        verify(playerTransform, times(1)).revertPosition();
    }

    @Test
    void testCollision_handlesCompleteOverlap() {
        // Given: Two entities setup
        setupTwoEntityCollisionScenario();

        // And: Entities at the same position (complete overlap)
        playerTransform.position.set(0, 0, 0);
        wallTransform.position.set(0, 0, 0);

        // When: The collision system updates
        collisionSystem.update(world, 0.1f);

        // Then: Collision should be detected
        verify(playerTransform, times(1)).revertPosition();
    }
}
