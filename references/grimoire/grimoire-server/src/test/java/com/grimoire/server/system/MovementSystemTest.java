package com.grimoire.server.system;

import com.grimoire.server.component.BoundingBox;
import com.grimoire.server.component.Position;
import com.grimoire.server.component.Solid;
import com.grimoire.server.component.Velocity;
import com.grimoire.server.component.Zone;
import com.grimoire.server.config.TestGameConfig;
import com.grimoire.server.ecs.ComponentManager;
import com.grimoire.server.ecs.EntityManager;
import com.grimoire.server.ecs.EcsWorld;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MovementSystemTest {
    
    private EcsWorld ecsWorld;
    private SpatialGridSystem spatialGridSystem;
    private MovementSystem movementSystem;
    
    @BeforeEach
    void setUp() {
        EntityManager entityManager = new EntityManager();
        ComponentManager componentManager = new ComponentManager();
        ecsWorld = new EcsWorld(entityManager, componentManager);
        spatialGridSystem = new SpatialGridSystem(ecsWorld, TestGameConfig.create());
        movementSystem = new MovementSystem(ecsWorld, spatialGridSystem);
    }
    
    @Test
    void testMovementUpdatesPosition() {
        String entityId = ecsWorld.createEntity();
        ecsWorld.addComponent(entityId, new Position(100, 100));
        ecsWorld.addComponent(entityId, new Velocity(10, 5));
        
        movementSystem.tick(1.0f);
        
        Position newPos = ecsWorld.getComponent(entityId, Position.class).orElse(null);
        assertNotNull(newPos);
        assertEquals(110.0, newPos.x(), 0.1);
        assertEquals(105.0, newPos.y(), 0.1);
    }
    
    @Test
    void testNoMovementWithZeroVelocity() {
        String entityId = ecsWorld.createEntity();
        ecsWorld.addComponent(entityId, new Position(100, 100));
        ecsWorld.addComponent(entityId, new Velocity(0, 0));
        
        movementSystem.tick(1.0f);
        
        Position newPos = ecsWorld.getComponent(entityId, Position.class).orElse(null);
        assertNotNull(newPos);
        assertEquals(100.0, newPos.x(), 0.1);
        assertEquals(100.0, newPos.y(), 0.1);
    }
    
    @Test
    void testCollisionWithSolidEntityBlocksMovement() {
        // Create moving entity
        String movingEntityId = ecsWorld.createEntity();
        ecsWorld.addComponent(movingEntityId, new Position(100, 100));
        ecsWorld.addComponent(movingEntityId, new Velocity(50, 0)); // Moving right, will end up at 150
        ecsWorld.addComponent(movingEntityId, new BoundingBox(10, 10));
        ecsWorld.addComponent(movingEntityId, new Zone("zone1"));
        
        // Create solid obstacle at position 150 (where entity would end up)
        // Entity bounding box at 150: [145, 155], obstacle at 155: [150, 160]
        // These would overlap at 150-155
        String obstacleId = ecsWorld.createEntity();
        ecsWorld.addComponent(obstacleId, new Position(155, 100));
        ecsWorld.addComponent(obstacleId, new BoundingBox(10, 10));
        ecsWorld.addComponent(obstacleId, new Solid());
        ecsWorld.addComponent(obstacleId, new Zone("zone1"));
        
        // Update spatial grid before movement
        spatialGridSystem.tick(0.05f);
        movementSystem.tick(1.0f);
        
        // Position should not have changed due to collision
        Position newPos = ecsWorld.getComponent(movingEntityId, Position.class).orElse(null);
        assertNotNull(newPos);
        assertEquals(100.0, newPos.x(), 0.1);
        
        // Velocity should be zeroed
        Velocity newVel = ecsWorld.getComponent(movingEntityId, Velocity.class).orElse(null);
        assertNotNull(newVel);
        assertEquals(0.0, newVel.dx(), 0.1);
    }
    
    @Test
    void testMovementAroundSolidEntity() {
        // Create moving entity
        String movingEntityId = ecsWorld.createEntity();
        ecsWorld.addComponent(movingEntityId, new Position(100, 100));
        ecsWorld.addComponent(movingEntityId, new Velocity(0, 50)); // Moving up, away from obstacle
        ecsWorld.addComponent(movingEntityId, new BoundingBox(10, 10));
        ecsWorld.addComponent(movingEntityId, new Zone("zone1"));
        
        // Create solid obstacle not in path
        String obstacleId = ecsWorld.createEntity();
        ecsWorld.addComponent(obstacleId, new Position(200, 100)); // Far to the right
        ecsWorld.addComponent(obstacleId, new BoundingBox(10, 10));
        ecsWorld.addComponent(obstacleId, new Solid());
        ecsWorld.addComponent(obstacleId, new Zone("zone1"));
        
        // Update spatial grid before movement
        spatialGridSystem.tick(0.05f);
        movementSystem.tick(1.0f);
        
        // Position should have changed since no collision
        Position newPos = ecsWorld.getComponent(movingEntityId, Position.class).orElse(null);
        assertNotNull(newPos);
        assertEquals(100.0, newPos.x(), 0.1);
        assertEquals(150.0, newPos.y(), 0.1);
    }
    
    @Test
    void testNoCollisionInDifferentZone() {
        // Create moving entity in zone1
        String movingEntityId = ecsWorld.createEntity();
        ecsWorld.addComponent(movingEntityId, new Position(100, 100));
        ecsWorld.addComponent(movingEntityId, new Velocity(50, 0));
        ecsWorld.addComponent(movingEntityId, new BoundingBox(10, 10));
        ecsWorld.addComponent(movingEntityId, new Zone("zone1"));
        
        // Create solid obstacle in zone2 (different zone)
        String obstacleId = ecsWorld.createEntity();
        ecsWorld.addComponent(obstacleId, new Position(120, 100));
        ecsWorld.addComponent(obstacleId, new BoundingBox(10, 10));
        ecsWorld.addComponent(obstacleId, new Solid());
        ecsWorld.addComponent(obstacleId, new Zone("zone2"));
        
        // Update spatial grid before movement
        spatialGridSystem.tick(0.05f);
        movementSystem.tick(1.0f);
        
        // Position should have changed since entities are in different zones
        Position newPos = ecsWorld.getComponent(movingEntityId, Position.class).orElse(null);
        assertNotNull(newPos);
        assertEquals(150.0, newPos.x(), 0.1);
    }
    
    @Test
    void testEntityWithoutBoundingBoxDoesNotCollide() {
        // Create moving entity WITHOUT bounding box
        String movingEntityId = ecsWorld.createEntity();
        ecsWorld.addComponent(movingEntityId, new Position(100, 100));
        ecsWorld.addComponent(movingEntityId, new Velocity(50, 0));
        ecsWorld.addComponent(movingEntityId, new Zone("zone1"));
        
        // Create solid obstacle in path
        String obstacleId = ecsWorld.createEntity();
        ecsWorld.addComponent(obstacleId, new Position(120, 100));
        ecsWorld.addComponent(obstacleId, new BoundingBox(10, 10));
        ecsWorld.addComponent(obstacleId, new Solid());
        ecsWorld.addComponent(obstacleId, new Zone("zone1"));
        
        // Update spatial grid before movement
        spatialGridSystem.tick(0.05f);
        movementSystem.tick(1.0f);
        
        // Position should have changed since moving entity has no bounding box
        Position newPos = ecsWorld.getComponent(movingEntityId, Position.class).orElse(null);
        assertNotNull(newPos);
        assertEquals(150.0, newPos.x(), 0.1);
    }
}
