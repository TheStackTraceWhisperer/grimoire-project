package com.grimoire.application.core.ecs;

import com.grimoire.domain.core.component.Component;
import com.grimoire.domain.core.component.Position;
import com.grimoire.domain.core.component.Stats;
import com.grimoire.domain.core.component.Velocity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

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
        manager.addComponent("e1", pos);

        Optional<Position> result = manager.getComponent("e1", Position.class);

        assertThat(result).contains(pos);
    }

    @Test
    void getComponentReturnsEmptyWhenAbsent() {
        assertThat(manager.getComponent("e1", Position.class)).isEmpty();
    }

    @Test
    void getComponentReturnsEmptyForWrongEntity() {
        manager.addComponent("e1", new Position(1, 2));

        assertThat(manager.getComponent("e2", Position.class)).isEmpty();
    }

    @Test
    void addComponentReplacesExisting() {
        manager.addComponent("e1", new Position(1, 2));
        var updated = new Position(3, 4);
        manager.addComponent("e1", updated);

        assertThat(manager.getComponent("e1", Position.class)).contains(updated);
    }

    @Test
    void hasComponentReturnsTrueWhenPresent() {
        manager.addComponent("e1", new Position(1, 2));

        assertThat(manager.hasComponent("e1", Position.class)).isTrue();
    }

    @Test
    void hasComponentReturnsFalseWhenAbsent() {
        assertThat(manager.hasComponent("e1", Position.class)).isFalse();
    }

    @Test
    void removeComponentRemovesIt() {
        manager.addComponent("e1", new Position(1, 2));
        manager.removeComponent("e1", Position.class);

        assertThat(manager.hasComponent("e1", Position.class)).isFalse();
    }

    @Test
    void removeComponentNoOpWhenAbsent() {
        manager.removeComponent("e1", Position.class);
        // should not throw
    }

    @Test
    void removeAllComponentsClearsEntity() {
        manager.addComponent("e1", new Position(1, 2));
        manager.addComponent("e1", new Stats(10, 10, 5, 5));

        manager.removeAllComponents("e1");

        assertThat(manager.hasComponent("e1", Position.class)).isFalse();
        assertThat(manager.hasComponent("e1", Stats.class)).isFalse();
    }

    @Test
    void removeAllComponentsDoesNotAffectOtherEntities() {
        manager.addComponent("e1", new Position(1, 2));
        manager.addComponent("e2", new Position(3, 4));

        manager.removeAllComponents("e1");

        assertThat(manager.hasComponent("e2", Position.class)).isTrue();
    }

    @Test
    void getEntitiesWithComponentReturnsMatchingEntities() {
        manager.addComponent("e1", new Position(1, 2));
        manager.addComponent("e2", new Position(3, 4));
        manager.addComponent("e3", new Stats(10, 10, 5, 5));

        assertThat(manager.getEntitiesWithComponent(Position.class))
                .containsExactlyInAnyOrder("e1", "e2");
    }

    @Test
    void getEntitiesWithComponentReturnsEmptyForUnknownType() {
        assertThat(manager.getEntitiesWithComponent(Position.class)).isEmpty();
    }

    @Test
    void getAllComponentsReturnsAllForEntity() {
        var pos = new Position(1, 2);
        var stats = new Stats(10, 10, 5, 5);
        manager.addComponent("e1", pos);
        manager.addComponent("e1", stats);

        Map<Class<? extends Component>, Component> all = manager.getAllComponents("e1");

        assertThat(all).hasSize(2);
        assertThat(all.get(Position.class)).isEqualTo(pos);
        assertThat(all.get(Stats.class)).isEqualTo(stats);
    }

    @Test
    void getAllComponentsReturnsEmptyForUnknownEntity() {
        assertThat(manager.getAllComponents("unknown")).isEmpty();
    }

    @Test
    void multipleComponentTypesPerEntity() {
        manager.addComponent("e1", new Position(1, 2));
        manager.addComponent("e1", new Velocity(0.5, -0.5));

        assertThat(manager.hasComponent("e1", Position.class)).isTrue();
        assertThat(manager.hasComponent("e1", Velocity.class)).isTrue();
    }
}
