package com.grimoire.application.core.ecs;

import com.grimoire.domain.core.component.Component;
import com.grimoire.domain.core.component.Position;
import com.grimoire.domain.core.component.Stats;
import com.grimoire.domain.core.component.Velocity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ComponentManagerTest {

    private ComponentManager manager;

    @BeforeEach
    void setUp() {
        manager = new ComponentManager();
    }

    @Test
    void addAndGetComponentRoundTrip() {
        var pos = new Position(1.0, 2.0);
        manager.addComponent(0, pos);

        Position result = manager.getComponent(0, Position.class);

        assertThat(result).isEqualTo(pos);
    }

    @Test
    void getComponentReturnsNullWhenAbsent() {
        assertThat(manager.getComponent(0, Position.class)).isNull();
    }

    @Test
    void getComponentReturnsNullForWrongEntity() {
        manager.addComponent(0, new Position(1, 2));

        assertThat(manager.getComponent(1, Position.class)).isNull();
    }

    @Test
    void addComponentReplacesExisting() {
        manager.addComponent(0, new Position(1, 2));
        var updated = new Position(3, 4);
        manager.addComponent(0, updated);

        assertThat(manager.getComponent(0, Position.class)).isEqualTo(updated);
    }

    @Test
    void hasComponentReturnsTrueWhenPresent() {
        manager.addComponent(0, new Position(1, 2));

        assertThat(manager.hasComponent(0, Position.class)).isTrue();
    }

    @Test
    void hasComponentReturnsFalseWhenAbsent() {
        assertThat(manager.hasComponent(0, Position.class)).isFalse();
    }

    @Test
    void removeComponentRemovesIt() {
        manager.addComponent(0, new Position(1, 2));
        manager.removeComponent(0, Position.class);

        assertThat(manager.hasComponent(0, Position.class)).isFalse();
    }

    @Test
    void removeComponentNoOpWhenAbsent() {
        manager.removeComponent(0, Position.class);
        // should not throw
    }

    @Test
    void removeAllComponentsClearsEntity() {
        manager.addComponent(0, new Position(1, 2));
        manager.addComponent(0, new Stats(10, 10, 5, 5));

        manager.removeAllComponents(0);

        assertThat(manager.hasComponent(0, Position.class)).isFalse();
        assertThat(manager.hasComponent(0, Stats.class)).isFalse();
    }

    @Test
    void removeAllComponentsDoesNotAffectOtherEntities() {
        manager.addComponent(0, new Position(1, 2));
        manager.addComponent(1, new Position(3, 4));

        manager.removeAllComponents(0);

        assertThat(manager.hasComponent(1, Position.class)).isTrue();
    }

    @Test
    void getAllComponentsReturnsAllForEntity() {
        var pos = new Position(1, 2);
        var stats = new Stats(10, 10, 5, 5);
        manager.addComponent(0, pos);
        manager.addComponent(0, stats);

        Map<Class<? extends Component>, Component> all = manager.getAllComponents(0);

        assertThat(all).hasSize(2);
        assertThat(all.get(Position.class)).isEqualTo(pos);
        assertThat(all.get(Stats.class)).isEqualTo(stats);
    }

    @Test
    void getAllComponentsReturnsEmptyForUnknownEntity() {
        assertThat(manager.getAllComponents(999)).isEmpty();
    }

    @Test
    void multipleComponentTypesPerEntity() {
        manager.addComponent(0, new Position(1, 2));
        manager.addComponent(0, new Velocity(0.5, -0.5));

        assertThat(manager.hasComponent(0, Position.class)).isTrue();
        assertThat(manager.hasComponent(0, Velocity.class)).isTrue();
    }

    @Test
    void directArrayAccessWorks() {
        var pos = new Position(5, 10);
        manager.addComponent(42, pos);

        assertThat(manager.getPositions()[42]).isEqualTo(pos);
    }
}
