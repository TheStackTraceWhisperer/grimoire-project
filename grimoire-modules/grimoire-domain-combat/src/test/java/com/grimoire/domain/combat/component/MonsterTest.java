package com.grimoire.domain.combat.component;

import com.grimoire.domain.combat.component.Monster.MonsterType;
import com.grimoire.domain.core.component.Component;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MonsterTest {

    @Test
    void creationWithExplicitXp() {
        var m = new Monster(MonsterType.WOLF, 99);

        assertThat(m.type()).isEqualTo(MonsterType.WOLF);
        assertThat(m.xpReward()).isEqualTo(99);
    }

    @Test
    void creationWithDefaultXp() {
        var m = new Monster(MonsterType.RAT);

        assertThat(m.type()).isEqualTo(MonsterType.RAT);
        assertThat(m.xpReward()).isEqualTo(10);
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
}
