package engine.services.world;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
public class WorldServiceIT {

    @Inject
    private WorldService worldService;

    // A simple component for testing
    private record TestComponent(String data) implements IComponent {}

    @AfterEach
    void tearDown() {
        // Clean up systems and entities between tests
        worldService.clearSystems();
        worldService.getEntitiesWith().forEach(worldService::destroyEntity);
    }

    @Test
    void testEntityAndComponentLifecycle() {
        // When: We create an entity and add a component
        int entity = worldService.createEntity();
        TestComponent component = new TestComponent("test-data");
        worldService.addComponent(entity, component);

        // Then: We should be able to retrieve the entity and its component
        assertTrue(worldService.hasComponent(entity, TestComponent.class), "Entity should have the component.");
        assertEquals(1, worldService.getEntitiesWith(TestComponent.class).size(), "Should find one entity with the component.");
        assertEquals(component, worldService.getComponent(entity, TestComponent.class), "Retrieved component should be the same as the one added.");

        // When: We remove the component
        worldService.removeComponent(entity, TestComponent.class);

        // Then: The entity should no longer have the component
        assertFalse(worldService.hasComponent(entity, TestComponent.class), "Component should have been removed.");

        // When: We destroy the entity
        worldService.destroyEntity(entity);

        // Then: The entity should no longer exist
        assertTrue(worldService.getEntitiesWith(TestComponent.class).isEmpty(), "No entities should exist after destruction.");
    }

    @Test
    void testSystemExecution() {
        // Given: An entity with a component
        int entity = worldService.createEntity();
        worldService.addComponent(entity, new TestComponent("initial"));

        // And: A system that modifies the component
        AtomicBoolean systemWasRun = new AtomicBoolean(false);
        ISystem testSystem = new ISystem() {
            @Override
            public void update(World world, float deltaTime) {
                world.getEntitiesWith(TestComponent.class).forEach(e -> {
                    world.addComponent(e, new TestComponent("modified"));
                });
                systemWasRun.set(true);
            }
        };
        worldService.addSystem(testSystem);

        // When: We update the world service
        worldService.update(0.0f);

        // Then: The system should have been executed
        assertTrue(systemWasRun.get(), "The system's update method should have been called.");

        // And: The component on the entity should have been modified by the system
        TestComponent modifiedComponent = worldService.getComponent(entity, TestComponent.class);
        assertEquals("modified", modifiedComponent.data, "Component data should have been modified by the system.");
    }
}
