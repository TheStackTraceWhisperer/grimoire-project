package com.grimoire.domain.combat.rule;

import com.grimoire.domain.core.component.Experience;
import com.grimoire.domain.core.component.Stats;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LevelingRulesTest {

    @Test
    void canLevelUpWhenXpMeetsThreshold() {
        assertThat(LevelingRules.canLevelUp(new Experience(100, 100))).isTrue();
    }

    @Test
    void canLevelUpWhenXpExceedsThreshold() {
        assertThat(LevelingRules.canLevelUp(new Experience(150, 100))).isTrue();
    }

    @Test
    void cannotLevelUpWhenXpBelowThreshold() {
        assertThat(LevelingRules.canLevelUp(new Experience(99, 100))).isFalse();
    }

    @Test
    void applyLevelUpRollsOverXp() {
        var exp = new Experience(130, 100);

        LevelingRules.applyLevelUp(exp);

        assertThat(exp.currentXp).isEqualTo(30);
    }

    @Test
    void applyLevelUpScalesThreshold() {
        var exp = new Experience(100, 100);

        LevelingRules.applyLevelUp(exp);

        assertThat(exp.xpToNextLevel).isEqualTo(150); // 100 × 1.5
    }

    @Test
    void applyLevelUpExactThreshold() {
        var exp = new Experience(100, 100);

        LevelingRules.applyLevelUp(exp);

        assertThat(exp.currentXp).isZero();
        assertThat(exp.xpToNextLevel).isEqualTo(150);
    }

    @Test
    void applyLevelUpRejectsInsufficientXp() {
        assertThatThrownBy(() -> LevelingRules.applyLevelUp(new Experience(50, 100)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void boostStatsIncreasesAttributes() {
        var stats = new Stats(80, 100, 10, 20);

        LevelingRules.boostStatsForLevelUp(stats);

        assertThat(stats.maxHp).isEqualTo(110);
        assertThat(stats.hp).isEqualTo(90);
        assertThat(stats.attack).isEqualTo(22);
        assertThat(stats.defense).isEqualTo(11);
    }

    @Test
    void boostStatsCapsHpAtMax() {
        var stats = new Stats(100, 100, 5, 10);

        LevelingRules.boostStatsForLevelUp(stats);

        assertThat(stats.hp).isEqualTo(110);
        assertThat(stats.maxHp).isEqualTo(110);
    }

    @Test
    void boostStatsWhenHpBelowMaxStillHeals() {
        var stats = new Stats(95, 100, 5, 10);

        LevelingRules.boostStatsForLevelUp(stats);

        assertThat(stats.hp).isEqualTo(105);
        assertThat(stats.maxHp).isEqualTo(110);
    }

    @Test
    void applyAllLevelUpsMultiple() {
        var exp = new Experience(500, 100);

        LevelingRules.applyAllLevelUps(exp);

        assertThat(exp.currentXp).isLessThan(exp.xpToNextLevel);
    }

    @Test
    void applyAllLevelUpsNone() {
        var exp = new Experience(50, 100);
        int originalXp = exp.currentXp;
        int originalThreshold = exp.xpToNextLevel;

        LevelingRules.applyAllLevelUps(exp);

        assertThat(exp.currentXp).isEqualTo(originalXp);
        assertThat(exp.xpToNextLevel).isEqualTo(originalThreshold);
    }

    @Test
    void addXp() {
        var exp = new Experience(50, 100);

        LevelingRules.addXp(exp, 30);

        assertThat(exp.currentXp).isEqualTo(80);
        assertThat(exp.xpToNextLevel).isEqualTo(100);
    }

    @Test
    void addXpRejectsNegative() {
        assertThatThrownBy(() -> LevelingRules.addXp(new Experience(0, 100), -1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void addXpZeroIsNoOp() {
        var exp = new Experience(50, 100);
        int before = exp.currentXp;

        LevelingRules.addXp(exp, 0);

        assertThat(exp.currentXp).isEqualTo(before);
    }

    @Test
    void countPendingLevelUps() {
        assertThat(LevelingRules.countPendingLevelUps(new Experience(50, 100))).isZero();
        assertThat(LevelingRules.countPendingLevelUps(new Experience(100, 100))).isEqualTo(1);
    }

    @Test
    void countPendingMultipleLevelUps() {
        assertThat(LevelingRules.countPendingLevelUps(new Experience(350, 100))).isEqualTo(2);
    }
}
