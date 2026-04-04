package com.grimoire.domain.combat.rule;

import com.grimoire.domain.core.component.Position;
import com.grimoire.domain.core.component.Stats;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CombatRulesTest {

    @Test
    void damageIsAttackMinusDefense() {
        var attacker = new Stats(100, 100, 5, 20);
        var target = new Stats(80, 80, 8, 10);

        assertThat(CombatRules.calculateDamage(attacker, target)).isEqualTo(12);
    }

    @Test
    void minimumDamageIsOne() {
        var attacker = new Stats(100, 100, 0, 1);
        var target = new Stats(80, 80, 50, 10);

        assertThat(CombatRules.calculateDamage(attacker, target)).isEqualTo(1);
    }

    @Test
    void applyDamageReducesHp() {
        var target = new Stats(50, 100, 5, 10);

        CombatRules.applyDamage(target, 20);

        assertThat(target.hp).isEqualTo(30);
        assertThat(target.maxHp).isEqualTo(100);
        assertThat(target.defense).isEqualTo(5);
        assertThat(target.attack).isEqualTo(10);
    }

    @Test
    void applyDamageFloorsAtZero() {
        var target = new Stats(10, 100, 5, 10);

        CombatRules.applyDamage(target, 999);

        assertThat(target.hp).isZero();
    }

    @Test
    void isDeadWhenHpZero() {
        assertThat(CombatRules.isDead(new Stats(0, 100, 0, 0))).isTrue();
    }

    @Test
    void isDeadWhenHpNegative() {
        assertThat(CombatRules.isDead(new Stats(-5, 100, 0, 0))).isTrue();
    }

    @Test
    void isNotDeadWhenHpPositive() {
        assertThat(CombatRules.isDead(new Stats(1, 100, 0, 0))).isFalse();
    }

    @Test
    void isInRangeWhenClose() {
        var a = new Position(0, 0);
        var b = new Position(3, 4);

        assertThat(CombatRules.isInRange(a, b, 5.0)).isTrue();
    }

    @Test
    void isInRangeExactBoundary() {
        var a = new Position(0, 0);
        var b = new Position(3, 4);

        assertThat(CombatRules.isInRange(a, b, 5.0)).isTrue();
    }

    @Test
    void isOutOfRange() {
        var a = new Position(0, 0);
        var b = new Position(3, 4);

        assertThat(CombatRules.isInRange(a, b, 4.9)).isFalse();
    }

    @Test
    void isInRangeSamePosition() {
        var a = new Position(10, 10);

        assertThat(CombatRules.isInRange(a, a, 0)).isTrue();
    }
}
