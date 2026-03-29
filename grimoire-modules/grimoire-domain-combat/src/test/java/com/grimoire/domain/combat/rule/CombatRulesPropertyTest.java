package com.grimoire.domain.combat.rule;

import com.grimoire.domain.core.component.Experience;
import com.grimoire.domain.core.component.Position;
import com.grimoire.domain.core.component.Stats;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.DoubleRange;
import net.jqwik.api.constraints.IntRange;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * jqwik property tests for combat and leveling domain invariants.
 */
class CombatRulesPropertyTest {

    // ── CombatRules ──

    @Property
    void damageIsAlwaysAtLeastOne(
            @ForAll @IntRange(min = 1, max = 1000) int hp,
            @ForAll @IntRange(min = 1, max = 1000) int maxHp,
            @ForAll @IntRange(min = 0, max = 500) int aDef,
            @ForAll @IntRange(min = 0, max = 500) int aAtk,
            @ForAll @IntRange(min = 0, max = 500) int tDef,
            @ForAll @IntRange(min = 0, max = 500) int tAtk) {
        var attacker = new Stats(hp, maxHp, aDef, aAtk);
        var target = new Stats(hp, maxHp, tDef, tAtk);

        assertThat(CombatRules.calculateDamage(attacker, target)).isGreaterThanOrEqualTo(1);
    }

    @Property
    void applyDamageNeverNegativeHp(
            @ForAll @IntRange(min = 0, max = 1000) int hp,
            @ForAll @IntRange(min = 1, max = 10000) int damage) {
        var target = new Stats(hp, 1000, 0, 0);

        var result = CombatRules.applyDamage(target, damage);

        assertThat(result.hp()).isGreaterThanOrEqualTo(0);
    }

    @Property
    void applyDamageNeverIncreasesHp(
            @ForAll @IntRange(min = 0, max = 1000) int hp,
            @ForAll @IntRange(min = 1, max = 10000) int damage) {
        var target = new Stats(hp, 1000, 0, 0);

        var result = CombatRules.applyDamage(target, damage);

        assertThat(result.hp()).isLessThanOrEqualTo(hp);
    }

    @Property
    void applyDamagePreservesNonHpFields(
            @ForAll @IntRange(min = 0, max = 1000) int hp,
            @ForAll @IntRange(min = 1, max = 1000) int maxHp,
            @ForAll @IntRange(min = 0, max = 500) int def,
            @ForAll @IntRange(min = 0, max = 500) int atk,
            @ForAll @IntRange(min = 1, max = 10000) int damage) {
        var target = new Stats(hp, maxHp, def, atk);

        var result = CombatRules.applyDamage(target, damage);

        assertThat(result.maxHp()).isEqualTo(maxHp);
        assertThat(result.defense()).isEqualTo(def);
        assertThat(result.attack()).isEqualTo(atk);
    }

    @Property
    void isDeadConsistentWithHpSign(@ForAll @IntRange(min = -100, max = 100) int hp) {
        var stats = new Stats(hp, 100, 0, 0);

        assertThat(CombatRules.isDead(stats)).isEqualTo(hp <= 0);
    }

    @Property
    void isInRangeIsReflexive(
            @ForAll @DoubleRange(min = -1000, max = 1000) double x,
            @ForAll @DoubleRange(min = -1000, max = 1000) double y) {
        var pos = new Position(x, y);

        assertThat(CombatRules.isInRange(pos, pos, 0)).isTrue();
    }

    @Property
    void isInRangeSymmetric(
            @ForAll @DoubleRange(min = -100, max = 100) double x1,
            @ForAll @DoubleRange(min = -100, max = 100) double y1,
            @ForAll @DoubleRange(min = -100, max = 100) double x2,
            @ForAll @DoubleRange(min = -100, max = 100) double y2,
            @ForAll @DoubleRange(min = 0, max = 500) double range) {
        var a = new Position(x1, y1);
        var b = new Position(x2, y2);

        assertThat(CombatRules.isInRange(a, b, range))
                .isEqualTo(CombatRules.isInRange(b, a, range));
    }

    // ── LevelingRules ──

    @Property
    void levelUpThresholdAlwaysIncreases(
            @ForAll @IntRange(min = 1, max = 10000) int xpToNext) {
        var exp = new Experience(xpToNext, xpToNext); // exactly enough to level

        var result = LevelingRules.applyLevelUp(exp);

        assertThat(result.xpToNextLevel()).isGreaterThan(xpToNext);
    }

    @Property
    void levelUpRolloverIsNonNegative(
            @ForAll @IntRange(min = 0, max = 10000) int excess,
            @ForAll @IntRange(min = 1, max = 10000) int threshold) {
        var exp = new Experience(threshold + excess, threshold);

        var result = LevelingRules.applyLevelUp(exp);

        assertThat(result.currentXp()).isGreaterThanOrEqualTo(0);
        assertThat(result.currentXp()).isEqualTo(excess);
    }

    @Property
    void applyAllLevelUpsTerminatesBelowThreshold(
            @ForAll @IntRange(min = 0, max = 50000) int xp,
            @ForAll @IntRange(min = 1, max = 1000) int threshold) {
        var exp = new Experience(xp, threshold);

        var result = LevelingRules.applyAllLevelUps(exp);

        assertThat(result.currentXp()).isLessThan(result.xpToNextLevel());
    }

    @Property
    void boostStatsNeverDecreasesAttributes(
            @ForAll @IntRange(min = 1, max = 1000) int maxHp,
            @ForAll @IntRange(min = 0, max = 500) int def,
            @ForAll @IntRange(min = 0, max = 500) int atk) {
        int hp = maxHp; // full health — worst case for the HP cap check
        var stats = new Stats(hp, maxHp, def, atk);

        var result = LevelingRules.boostStatsForLevelUp(stats);

        assertThat(result.maxHp()).isGreaterThan(maxHp);
        assertThat(result.attack()).isGreaterThan(atk);
        assertThat(result.defense()).isGreaterThan(def);
        assertThat(result.hp()).isGreaterThanOrEqualTo(hp);
    }

    @Property
    void boostStatsHpNeverExceedsNewMax(
            @ForAll @IntRange(min = 0, max = 1000) int hp,
            @ForAll @IntRange(min = 1, max = 1000) int maxHp,
            @ForAll @IntRange(min = 0, max = 500) int def,
            @ForAll @IntRange(min = 0, max = 500) int atk) {
        var stats = new Stats(hp, maxHp, def, atk);

        var result = LevelingRules.boostStatsForLevelUp(stats);

        assertThat(result.hp()).isLessThanOrEqualTo(result.maxHp());
    }

    @Property
    void addXpPreservesThreshold(
            @ForAll @IntRange(min = 0, max = 10000) int xp,
            @ForAll @IntRange(min = 1, max = 10000) int threshold,
            @ForAll @IntRange(min = 0, max = 10000) int gain) {
        var exp = new Experience(xp, threshold);

        var result = LevelingRules.addXp(exp, gain);

        assertThat(result.xpToNextLevel()).isEqualTo(threshold);
        assertThat(result.currentXp()).isEqualTo(xp + gain);
    }
}
