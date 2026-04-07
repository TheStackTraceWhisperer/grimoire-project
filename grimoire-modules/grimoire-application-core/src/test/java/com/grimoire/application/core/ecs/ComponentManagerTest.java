package com.grimoire.application.core.ecs;

import com.grimoire.domain.core.component.Component;
import com.grimoire.domain.core.component.Dead;
import com.grimoire.domain.core.component.Dirty;
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

    // ── Signature tests ──

    @Test
    void signatureSetOnAddComponent() {
        manager.addComponent(0, new Position(1, 2));

        long sig = manager.getSignatures()[0];
        assertThat(sig & ComponentManager.BIT_POSITION).isNotZero();
    }

    @Test
    void signatureClearedOnRemoveComponent() {
        manager.addComponent(0, new Position(1, 2));
        manager.removeComponent(0, Position.class);

        long sig = manager.getSignatures()[0];
        assertThat(sig & ComponentManager.BIT_POSITION).isZero();
    }

    @Test
    void signatureZeroedOnRemoveAllComponents() {
        manager.addComponent(0, new Position(1, 2));
        manager.addComponent(0, new Stats(10, 10, 5, 5));
        manager.removeAllComponents(0);

        assertThat(manager.getSignatures()[0]).isZero();
    }

    @Test
    void multipleSignatureBitsSetCorrectly() {
        manager.addComponent(0, new Position(1, 2));
        manager.addComponent(0, new Velocity(3, 4));

        long sig = manager.getSignatures()[0];
        assertThat(sig & ComponentManager.BIT_POSITION).isNotZero();
        assertThat(sig & ComponentManager.BIT_VELOCITY).isNotZero();
        assertThat(sig & ComponentManager.BIT_STATS).isZero();
    }

    // ── Typed add method tests ──

    @Test
    void typedAddPositionWipesAndSetsFields() {
        manager.addPosition(0, 10, 20);

        Position pos = manager.getPositions()[0];
        assertThat(pos).isNotNull();
        assertThat(pos.x).isEqualTo(10.0);
        assertThat(pos.y).isEqualTo(20.0);
        assertThat(manager.getSignatures()[0] & ComponentManager.BIT_POSITION).isNotZero();
    }

    @Test
    void typedAddPositionReusesExistingObject() {
        manager.addPosition(0, 10, 20);
        Position first = manager.getPositions()[0];

        manager.addPosition(0, 30, 40);
        Position second = manager.getPositions()[0];

        assertThat(second).isSameAs(first);
        assertThat(second.x).isEqualTo(30.0);
        assertThat(second.y).isEqualTo(40.0);
    }

    @Test
    void typedAddDirtySetsSignature() {
        manager.addDirty(0, 42L);

        assertThat(manager.getDirties()[0]).isNotNull();
        assertThat(manager.getDirties()[0].tick).isEqualTo(42L);
        assertThat(manager.getSignatures()[0] & ComponentManager.BIT_DIRTY).isNotZero();
    }

    @Test
    void typedAddDeadSetsSignature() {
        manager.addDead(0, 5);

        assertThat(manager.getDeads()[0]).isNotNull();
        assertThat(manager.getDeads()[0].killerId).isEqualTo(5);
        assertThat(manager.getSignatures()[0] & ComponentManager.BIT_DEAD).isNotZero();
    }

    @Test
    void recycledSlotHasNoStaleData() {
        // Simulate recycling: add Position, then removeAll, then add fresh
        manager.addPosition(0, 100, 200);
        manager.removeAllComponents(0);

        // Signature should be zero
        assertThat(manager.getSignatures()[0]).isZero();

        // Adding fresh data should wipe the old fields
        manager.addPosition(0, 1, 2);
        Position pos = manager.getPositions()[0];
        assertThat(pos.x).isEqualTo(1.0);
        assertThat(pos.y).isEqualTo(2.0);
    }
}
