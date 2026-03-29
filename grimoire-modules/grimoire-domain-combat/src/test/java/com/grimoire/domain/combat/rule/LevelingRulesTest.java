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

        var result = LevelingRules.applyLevelUp(exp);

        assertThat(result.currentXp()).isEqualTo(30);
    }

    @Test
    void applyLevelUpScalesThreshold() {
        var exp = new Experience(100, 100);

        var result = LevelingRules.applyLevelUp(exp);

        assertThat(result.xpToNextLevel()).isEqualTo(150); // 100 × 1.5
    }

    @Test
    void applyLevelUpExactThreshold() {
        var exp = new Experience(100, 100);

        var result = LevelingRules.applyLevelUp(exp);

        assertThat(result.currentXp()).isZero();
        assertThat(result.xpToNextLevel()).isEqualTo(150);
    }

    @Test
    void applyLevelUpRejectsInsufficientXp() {
        assertThatThrownBy(() -> LevelingRules.applyLevelUp(new Experience(50, 100)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void boostStatsIncreasesAttributes() {
        var stats = new Stats(80, 100, 10, 20);

        var result = LevelingRules.boostStatsForLevelUp(stats);

        assertThat(result.maxHp()).isEqualTo(110);
        assertThat(result.hp()).isEqualTo(90);
        assertThat(result.attack()).isEqualTo(22);
        assertThat(result.defense()).isEqualTo(11);
    }

    @Test
    void boostStatsCapsHpAtMax() {
        var stats = new Stats(100, 100, 5, 10);

        var result = LevelingRules.boostStatsForLevelUp(stats);

        assertThat(result.hp()).isEqualTo(110);
        assertThat(result.maxHp()).isEqualTo(110);
    }

    @Test
    void boostStatsWhenHpBelowMaxStillHeals() {
        var stats = new Stats(95, 100, 5, 10);

        var result = LevelingRules.boostStatsForLevelUp(stats);

        assertThat(result.hp()).isEqualTo(105);
        assertThat(result.maxHp()).isEqualTo(110);
    }

    @Test
    void applyAllLevelUpsMultiple() {
        // 500 XP, threshold 100 → should level up multiple times
        var exp = new Experience(500, 100);

        var result = LevelingRules.applyAllLevelUps(exp);

        assertThat(result.currentXp()).isLessThan(result.xpToNextLevel());
    }

    @Test
    void applyAllLevelUpsNone() {
        var exp = new Experience(50, 100);

        var result = LevelingRules.applyAllLevelUps(exp);

        assertThat(result).isEqualTo(exp);
    }

    @Test
    void addXp() {
        var exp = new Experience(50, 100);

        var result = LevelingRules.addXp(exp, 30);

        assertThat(result.currentXp()).isEqualTo(80);
        assertThat(result.xpToNextLevel()).isEqualTo(100);
    }

    @Test
    void addXpRejectsNegative() {
        assertThatThrownBy(() -> LevelingRules.addXp(new Experience(0, 100), -1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void addXpZeroIsNoOp() {
        var exp = new Experience(50, 100);

        assertThat(LevelingRules.addXp(exp, 0)).isEqualTo(exp);
    }

    @Test
    void countPendingLevelUps() {
        assertThat(LevelingRules.countPendingLevelUps(new Experience(50, 100))).isZero();
        assertThat(LevelingRules.countPendingLevelUps(new Experience(100, 100))).isEqualTo(1);
    }

    @Test
    void countPendingMultipleLevelUps() {
        // 350 XP, threshold 100 → level 1 (100→150, remaining 250),
        // level 2 (150→225, remaining 100), not enough for 225
        assertThat(LevelingRules.countPendingLevelUps(new Experience(350, 100))).isEqualTo(2);
    }
}
