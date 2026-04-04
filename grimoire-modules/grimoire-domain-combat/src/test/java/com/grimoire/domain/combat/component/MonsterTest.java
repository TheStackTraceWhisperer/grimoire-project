package com.grimoire.domain.combat.component;

import com.grimoire.domain.combat.component.Monster.MonsterType;
import com.grimoire.domain.core.component.Component;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MonsterTest {

    @Test
    void creationWithExplicitXp() {
        var m = new Monster(MonsterType.WOLF, 99);

        assertThat(m.type).isEqualTo(MonsterType.WOLF);
        assertThat(m.xpReward).isEqualTo(99);
    }

    @Test
    void creationWithDefaultXp() {
        var m = new Monster(MonsterType.RAT);

        assertThat(m.type).isEqualTo(MonsterType.RAT);
        assertThat(m.xpReward).isEqualTo(10);
    }

    @Test
    void noArgConstructor() {
        var m = new Monster();

        assertThat(m.type).isNull();
        assertThat(m.xpReward).isZero();
    }

    @Test
    void implementsComponent() {
        assertThat(new Monster(MonsterType.BAT)).isInstanceOf(Component.class);
    }

    @Test
    void allMonsterTypesHaveDefaultXp() {
        for (MonsterType type : MonsterType.values()) {
            assertThat(type.defaultXp()).isPositive();
        }
    }

    @Test
    void monsterTypeCount() {
        assertThat(MonsterType.values()).hasSize(4);
    }

    @Test
    void updateChangesFields() {
        var m = new Monster(MonsterType.RAT, 10);

        m.update(MonsterType.SKELETON, 50);

        assertThat(m.type).isEqualTo(MonsterType.SKELETON);
        assertThat(m.xpReward).isEqualTo(50);
    }

    @Test
    void equalsSameValues() {
        assertThat(new Monster(MonsterType.WOLF, 25))
                .isEqualTo(new Monster(MonsterType.WOLF, 25));
    }

    @Test
    void equalsSameInstance() {
        var m = new Monster(MonsterType.BAT);

        assertThat(m).isEqualTo(m);
    }

    @Test
    void notEqualsDifferentType() {
        assertThat(new Monster(MonsterType.WOLF, 25))
                .isNotEqualTo(new Monster(MonsterType.RAT, 25));
    }

    @Test
    void notEqualsDifferentXp() {
        assertThat(new Monster(MonsterType.WOLF, 25))
                .isNotEqualTo(new Monster(MonsterType.WOLF, 99));
    }

    @Test
    void notEqualsDifferentObjectType() {
        assertThat(new Monster(MonsterType.WOLF)).isNotEqualTo("not a monster");
    }

    @Test
    void hashCodeConsistentForEqualObjects() {
        assertThat(new Monster(MonsterType.WOLF, 25).hashCode())
                .isEqualTo(new Monster(MonsterType.WOLF, 25).hashCode());
    }

    @Test
    void hashCodeWithNullType() {
        var m = new Monster();
        // Should not throw
        assertThat(m.hashCode()).isNotNull();
    }

    @Test
    void toStringContainsFields() {
        var m = new Monster(MonsterType.WOLF, 25);

        assertThat(m.toString()).contains("WOLF").contains("25");
    }

    @Test
    void defaultXpValues() {
        assertThat(MonsterType.RAT.defaultXp()).isEqualTo(10);
        assertThat(MonsterType.BAT.defaultXp()).isEqualTo(15);
        assertThat(MonsterType.WOLF.defaultXp()).isEqualTo(25);
        assertThat(MonsterType.SKELETON.defaultXp()).isEqualTo(40);
    }
}
