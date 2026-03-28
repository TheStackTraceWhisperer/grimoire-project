package engine.services.world;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WorldTest {

    private World world;

    // Test components
    private record ComponentA() implements IComponent {}
    private record ComponentB() implements IComponent {}

    @BeforeEach
    void setUp() {
        world = new World();
    }

    @Test
    void createEntity_returnsIncrementingIds() {
        int entity1 = world.createEntity();
        int entity2 = world.createEntity();
        assertEquals(0, entity1);
        assertEquals(1, entity2);
    }

    @Test
    void destroyEntity_doesNotRecycleEntityId() {
        int entity1 = world.createEntity();
        world.destroyEntity(entity1);
        int entity2 = world.createEntity(); // The next ID should be 1, not the recycled 0.
        assertNotEquals(entity1, entity2, "Destroyed entity ID should not be immediately recycled.");
        assertEquals(1, entity2);
    }

    @Test
    void addAndGetComponent_managesComponentLifecycle() {
        // Given
        int entity = world.createEntity();
        ComponentA component = new ComponentA();

        // When
        world.addComponent(entity, component);

        // Then
        assertTrue(world.hasComponent(entity, ComponentA.class), "World should report entity has component.");
        assertSame(component, world.getComponent(entity, ComponentA.class), "Retrieved component should be the same instance.");

        // When
        world.removeComponent(entity, ComponentA.class);

        // Then
        assertFalse(world.hasComponent(entity, ComponentA.class), "World should report component is removed.");
        assertNull(world.getComponent(entity, ComponentA.class), "getComponent should return null for removed component.");
    }

    @Test
    void getEntitiesWith_returnsAllEntitiesWithAtLeastTheGivenComponents() {
        // Given: Three entities with different component combinations
        int entityA = world.createEntity();
        world.addComponent(entityA, new ComponentA());

        int entityB = world.createEntity();
        world.addComponent(entityB, new ComponentB());

        int entityAB = world.createEntity();
        world.addComponent(entityAB, new ComponentA());
        world.addComponent(entityAB, new ComponentB());

        // When & Then: We query for entities with different signatures
        var entitiesWithA = world.getEntitiesWith(ComponentA.class);
        assertEquals(2, entitiesWithA.size(), "Should find 2 entities with at least ComponentA.");
        assertTrue(entitiesWithA.contains(entityA));
        assertTrue(entitiesWithA.contains(entityAB));

        var entitiesWithB = world.getEntitiesWith(ComponentB.class);
        assertEquals(2, entitiesWithB.size(), "Should find 2 entities with at least ComponentB.");
        assertTrue(entitiesWithB.contains(entityB));
        assertTrue(entitiesWithB.contains(entityAB));

        var entitiesWithAB = world.getEntitiesWith(ComponentA.class, ComponentB.class);
        assertEquals(1, entitiesWithAB.size(), "Should find 1 entity with both ComponentA and ComponentB.");
        assertTrue(entitiesWithAB.contains(entityAB));
    }

    @Test
    void destroyEntity_removesAllComponentsAndSignature() {
        // Given
        int entity = world.createEntity();
        world.addComponent(entity, new ComponentA());
        world.addComponent(entity, new ComponentB());
        assertFalse(world.getEntitiesWith(ComponentA.class, ComponentB.class).isEmpty());

        // When
        world.destroyEntity(entity);

        // Then
        assertFalse(world.hasComponent(entity, ComponentA.class), "ComponentA should be gone after entity destruction.");
        assertFalse(world.hasComponent(entity, ComponentB.class), "ComponentB should be gone after entity destruction.");
        assertTrue(world.getEntitiesWith(ComponentA.class, ComponentB.class).isEmpty(), "No entities should match signature after destruction.");
    }
}
