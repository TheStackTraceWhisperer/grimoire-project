package com.grimoire.application.core.system;

import com.grimoire.application.core.ecs.ComponentManager;
import com.grimoire.application.core.ecs.EcsWorld;
import com.grimoire.application.core.ecs.EntityManager;
import com.grimoire.application.core.port.GameConfig;
import com.grimoire.domain.core.component.BoundingBox;
import com.grimoire.domain.core.component.Dirty;
import com.grimoire.domain.core.component.Position;
import com.grimoire.domain.core.component.Solid;
import com.grimoire.domain.core.component.Velocity;
import com.grimoire.domain.core.component.Zone;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class MovementSystemTest {

    private EcsWorld world;
    private SpatialGridSystem spatialGridSystem;
    private MovementSystem system;

    @BeforeEach
    void setUp() {
        world = new EcsWorld(new EntityManager(), new ComponentManager());
        GameConfig config = new GameConfig() {
        };
        spatialGridSystem = new SpatialGridSystem(world, config);
        system = new MovementSystem(world, spatialGridSystem);
    }

    @Test
    void entityMovesWithVelocity() {
        // velocity * TICK_DELTA(0.05) → 10*0.05=0.5, 5*0.05=0.25
        int entity = world.createEntity();
        world.addComponent(entity, new Position(0, 0));
        world.addComponent(entity, new Velocity(10, 5));

        system.tick(0L);

        Position pos = world.getComponent(entity, Position.class);
        assertThat(pos.x).isCloseTo(0.5, within(0.01));
        assertThat(pos.y).isCloseTo(0.25, within(0.01));
    }

    @Test
    void entityMarkedDirtyAfterMove() {
        int entity = world.createEntity();
        world.addComponent(entity, new Position(0, 0));
        world.addComponent(entity, new Velocity(10, 0));

        system.tick(0L);

        assertThat(world.hasComponent(entity, Dirty.class)).isTrue();
    }

    @Test
    void noMovementBelowThreshold() {
        int entity = world.createEntity();
        world.addComponent(entity, new Position(5, 5));
        world.addComponent(entity, new Velocity(0.001, 0.001));

        system.tick(0L);

        Position pos = world.getComponent(entity, Position.class);
        assertThat(pos.x).isEqualTo(5.0);
        assertThat(pos.y).isEqualTo(5.0);
    }

    @Test
    void fixedTickDeltaApplied() {
        // TICK_DELTA=0.05, velocity 100 → 100*0.05=5.0
        int entity = world.createEntity();
        world.addComponent(entity, new Position(0, 0));
        world.addComponent(entity, new Velocity(100, 0));

        system.tick(0L);

        Position pos = world.getComponent(entity, Position.class);
        assertThat(pos.x).isCloseTo(5.0, within(0.01));
    }

    @Test
    void collisionStopsEntity() {
        // Create a solid obstacle in the grid
        int obstacle = world.createEntity();
        world.addComponent(obstacle, new Position(10, 0));
        world.addComponent(obstacle, new BoundingBox(4, 4));
        world.addComponent(obstacle, new Solid());
        world.addComponent(obstacle, new Zone("default"));

        // Rebuild the spatial grid
        spatialGridSystem.tick(0L);

        // velocity 200 → 200*0.05=10.0, landing on the obstacle
        int mover = world.createEntity();
        world.addComponent(mover, new Position(0, 0));
        world.addComponent(mover, new Velocity(200, 0));
        world.addComponent(mover, new BoundingBox(4, 4));

        system.tick(0L);

        // Entity should be stopped (velocity zeroed) due to AABB overlap
        Velocity vel = world.getComponent(mover, Velocity.class);
        assertThat(vel.dx).isEqualTo(0.0);
    }

    @Test
    void entityWithoutBoundingBoxDoesNotCollide() {
        // Create a solid obstacle
        int obstacle = world.createEntity();
        world.addComponent(obstacle, new Position(10, 0));
        world.addComponent(obstacle, new BoundingBox(4, 4));
        world.addComponent(obstacle, new Solid());
        world.addComponent(obstacle, new Zone("default"));

        spatialGridSystem.tick(0L);

        // velocity 200 → 200*0.05=10.0
        int mover = world.createEntity();
        world.addComponent(mover, new Position(0, 0));
        world.addComponent(mover, new Velocity(200, 0));
        // No BoundingBox — should pass through

        system.tick(0L);

        Position pos = world.getComponent(mover, Position.class);
        assertThat(pos.x).isCloseTo(10.0, within(0.01));
    }

    @Test
    void entityWithoutPositionIsSkipped() {
        int entity = world.createEntity();
        world.addComponent(entity, new Velocity(10, 0));
        // No Position

        system.tick(0L);

        assertThat(world.hasComponent(entity, Position.class)).isFalse();
    }

    @Test
    void multipleEntitiesMoveIndependently() {
        // velocity * TICK_DELTA(0.05)
        int e1 = world.createEntity();
        world.addComponent(e1, new Position(0, 0));
        world.addComponent(e1, new Velocity(200, 0)); // 200*0.05=10.0

        int e2 = world.createEntity();
        world.addComponent(e2, new Position(0, 0));
        world.addComponent(e2, new Velocity(0, 400)); // 400*0.05=20.0

        system.tick(0L);

        Position p1 = world.getComponent(e1, Position.class);
        Position p2 = world.getComponent(e2, Position.class);
        assertThat(p1.x).isCloseTo(10.0, within(0.01));
        assertThat(p2.y).isCloseTo(20.0, within(0.01));
    }
}
