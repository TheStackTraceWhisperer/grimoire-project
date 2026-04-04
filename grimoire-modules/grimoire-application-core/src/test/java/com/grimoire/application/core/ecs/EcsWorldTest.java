package com.grimoire.application.core.ecs;

import com.grimoire.domain.core.component.Component;
import com.grimoire.domain.core.component.Position;
import com.grimoire.domain.core.component.Stats;
import com.grimoire.domain.core.component.Velocity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EcsWorldTest {

    private EcsWorld world;

    @BeforeEach
    void setUp() {
        world = new EcsWorld(new EntityManager(), new ComponentManager());
    }

    // ── Entity lifecycle ──

    @Test
    void createEntityReturnsNonNegativeId() {
        assertThat(world.createEntity()).isGreaterThanOrEqualTo(0);
    }

    @Test
    void createdEntityExists() {
        int id = world.createEntity();

        assertThat(world.entityExists(id)).isTrue();
    }

    @Test
    void destroyEntityRemovesEntityAndComponents() {
        int id = world.createEntity();
        world.addComponent(id, new Position(1, 2));

        world.destroyEntity(id);

        assertThat(world.entityExists(id)).isFalse();
        assertThat(world.getComponent(id, Position.class)).isNull();
    }

    // ── Component CRUD ──

    @Test
    void addAndGetComponent() {
        int id = world.createEntity();
        var pos = new Position(10, 20);
        world.addComponent(id, pos);

        assertThat(world.getComponent(id, Position.class)).isEqualTo(pos);
    }

    @Test
    void hasComponent() {
        int id = world.createEntity();
        world.addComponent(id, new Position(0, 0));

        assertThat(world.hasComponent(id, Position.class)).isTrue();
        assertThat(world.hasComponent(id, Stats.class)).isFalse();
    }

    @Test
    void removeComponent() {
        int id = world.createEntity();
        world.addComponent(id, new Position(0, 0));
        world.removeComponent(id, Position.class);

        assertThat(world.hasComponent(id, Position.class)).isFalse();
    }

    @Test
    void getAllComponents() {
        int id = world.createEntity();
        var pos = new Position(1, 2);
        var vel = new Velocity(3, 4);
        world.addComponent(id, pos);
        world.addComponent(id, vel);

        Map<Class<? extends Component>, Component> all = world.getAllComponents(id);

        assertThat(all).hasSize(2);
        assertThat(all.get(Position.class)).isEqualTo(pos);
        assertThat(all.get(Velocity.class)).isEqualTo(vel);
    }

    // ── Tick ──

    @Test
    void initialTickIsZero() {
        assertThat(world.getCurrentTick()).isZero();
    }

    @Test
    void incrementTickAdvancesCounter() {
        world.incrementTick();
        world.incrementTick();

        assertThat(world.getCurrentTick()).isEqualTo(2);
    }

    // ── Prefabs ──

    @Test
    void registerAndCreateFromPrefab() {
        Prefab prefab = new Prefab("test-mob")
                .addComponent(new Position(5, 5))
                .addComponent(new Stats(100, 100, 10, 10));

        world.registerPrefab(prefab);
        int id = world.createEntityFromPrefab("test-mob");

        assertThat(world.entityExists(id)).isTrue();
        assertThat(world.getComponent(id, Position.class)).isNotNull();
        assertThat(world.getComponent(id, Stats.class)).isNotNull();
    }

    @Test
    void createFromPrefabWithCustomizer() {
        Prefab prefab = new Prefab("custom")
                .addComponent(new Position(0, 0))
                .addComponent(new Stats(50, 50, 5, 5));

        world.registerPrefab(prefab);

        int id = world.createEntityFromPrefab("custom", c -> {
            if (c instanceof Position) {
                return new Position(99, 99);
            }
            return c;
        });

        assertThat(world.getComponent(id, Position.class))
                .isEqualTo(new Position(99, 99));
        assertThat(world.getComponent(id, Stats.class))
                .isEqualTo(new Stats(50, 50, 5, 5));
    }

    @Test
    void createFromPrefabCustomizerCanFilterComponents() {
        Prefab prefab = new Prefab("filter")
                .addComponent(new Position(1, 1))
                .addComponent(new Velocity(2, 2));

        world.registerPrefab(prefab);

        int id = world.createEntityFromPrefab("filter", c -> {
            if (c instanceof Velocity) {
                return null; // skip
            }
            return c;
        });

        assertThat(world.hasComponent(id, Position.class)).isTrue();
        assertThat(world.hasComponent(id, Velocity.class)).isFalse();
    }

    @Test
    void createFromPrefabThrowsForUnknownPrefab() {
        assertThatThrownBy(() -> world.createEntityFromPrefab("missing"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("missing");
    }

    @Test
    void createFromPrefabThrowsForNullName() {
        assertThatThrownBy(() -> world.createEntityFromPrefab(null))
                .isInstanceOf(NullPointerException.class);
    }

    // ── Int-based entity ID ──

    @Test
    void entitiesAreSequentialInts() {
        int id1 = world.createEntity();
        int id2 = world.createEntity();

        assertThat(id2).isEqualTo(id1 + 1);
    }

    @Test
    void maxEntityIdTracksHighWaterMark() {
        world.createEntity();
        world.createEntity();

        assertThat(world.getMaxEntityId()).isEqualTo(2);
    }
}
