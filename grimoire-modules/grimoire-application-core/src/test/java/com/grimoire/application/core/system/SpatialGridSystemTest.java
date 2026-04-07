package com.grimoire.application.core.system;

import com.grimoire.application.core.ecs.ComponentManager;
import com.grimoire.application.core.ecs.EcsWorld;
import com.grimoire.application.core.ecs.EntityManager;
import com.grimoire.application.core.port.GameConfig;
import com.grimoire.domain.core.component.Position;
import com.grimoire.domain.core.component.Solid;
import com.grimoire.domain.core.component.Zone;
import com.grimoire.domain.navigation.spatial.SpatialGrid;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SpatialGridSystemTest {

    private EcsWorld world;
    private SpatialGridSystem system;

    @BeforeEach
    void setUp() {
        world = new EcsWorld(new EntityManager(), new ComponentManager());
        GameConfig config = new GameConfig() {
        };
        system = new SpatialGridSystem(world, config);
    }

    @Test
    void solidEntityIsAddedToGrid() {
        int entity = world.createEntity();
        world.addComponent(entity, new Position(10, 20));
        world.addComponent(entity, new Solid());
        world.addComponent(entity, new Zone("zone1"));

        system.tick(0L);

        SpatialGrid grid = system.getGrid();
        Set<Integer> nearby = grid.getNearbyEntities(10, 20, "zone1");
        assertThat(nearby).contains(entity);
    }

    @Test
    void nonSolidEntityIsNotInGrid() {
        int entity = world.createEntity();
        world.addComponent(entity, new Position(10, 20));
        // No Solid component

        system.tick(0L);

        SpatialGrid grid = system.getGrid();
        Set<Integer> nearby = grid.getNearbyEntities(10, 20, "default");
        assertThat(nearby).doesNotContain(entity);
    }

    @Test
    void gridIsClearedEachTick() {
        int entity = world.createEntity();
        world.addComponent(entity, new Position(10, 20));
        world.addComponent(entity, new Solid());

        system.tick(0L);
        assertThat(system.getGrid().getEntityCount()).isEqualTo(1);

        // Remove the Solid component and re-tick
        world.removeComponent(entity, Solid.class);
        system.tick(0L);
        assertThat(system.getGrid().getEntityCount()).isZero();
    }

    @Test
    void entityWithoutPositionIsSkipped() {
        int entity = world.createEntity();
        world.addComponent(entity, new Solid());
        // No Position component

        system.tick(0L);

        assertThat(system.getGrid().getEntityCount()).isZero();
    }

    @Test
    void defaultZoneUsedWhenZoneComponentAbsent() {
        int entity = world.createEntity();
        world.addComponent(entity, new Position(5, 5));
        world.addComponent(entity, new Solid());

        system.tick(0L);

        Set<Integer> nearby = system.getGrid().getNearbyEntities(5, 5, "default");
        assertThat(nearby).contains(entity);
    }

    @Test
    void removeEntityFromGrid() {
        int entity = world.createEntity();
        world.addComponent(entity, new Position(10, 20));
        world.addComponent(entity, new Solid());

        system.tick(0L);
        assertThat(system.getGrid().getEntityCount()).isEqualTo(1);

        system.removeEntity(entity);
        assertThat(system.getGrid().getEntityCount()).isZero();
    }
}
