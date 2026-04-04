package com.grimoire.domain.core.component;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.DoubleRange;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.StringLength;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * jqwik property tests verifying domain invariants for core components.
 */
class ComponentPropertyTest {

    // ── Position ──

    @Property
    void positionPreservesCoordinates(@ForAll double x, @ForAll double y) {
        var pos = new Position(x, y);

        assertThat(pos.x).isEqualTo(x);
        assertThat(pos.y).isEqualTo(y);
    }

    @Property
    void positionEqualityByValue(@ForAll double x, @ForAll double y) {
        assertThat(new Position(x, y)).isEqualTo(new Position(x, y));
    }

    // ── Stats ──

    @Property
    void statsPreservesAllFields(
            @ForAll int hp,
            @ForAll @IntRange(min = 1) int maxHp,
            @ForAll @IntRange(min = 0) int defense,
            @ForAll @IntRange(min = 0) int attack) {
        var stats = new Stats(hp, maxHp, defense, attack);

        assertThat(stats.hp).isEqualTo(hp);
        assertThat(stats.maxHp).isEqualTo(maxHp);
        assertThat(stats.defense).isEqualTo(defense);
        assertThat(stats.attack).isEqualTo(attack);
    }

    @Property
    void statsEqualityByValue(
            @ForAll int hp,
            @ForAll int maxHp,
            @ForAll int defense,
            @ForAll int attack) {
        assertThat(new Stats(hp, maxHp, defense, attack))
                .isEqualTo(new Stats(hp, maxHp, defense, attack));
    }

    // ── Experience ──

    @Property
    void experiencePreservesValues(
            @ForAll @IntRange(min = 0) int currentXp,
            @ForAll @IntRange(min = 1) int xpToNextLevel) {
        var exp = new Experience(currentXp, xpToNextLevel);

        assertThat(exp.currentXp).isEqualTo(currentXp);
        assertThat(exp.xpToNextLevel).isEqualTo(xpToNextLevel);
    }

    // ── Velocity ──

    @Property
    void velocityPreservesComponents(@ForAll double dx, @ForAll double dy) {
        var vel = new Velocity(dx, dy);

        assertThat(vel.dx).isEqualTo(dx);
        assertThat(vel.dy).isEqualTo(dy);
    }

    // ── BoundingBox ──

    @Property
    void boundingBoxPreservesDimensions(
            @ForAll @DoubleRange(min = 0.0) double width,
            @ForAll @DoubleRange(min = 0.0) double height) {
        var bb = new BoundingBox(width, height);

        assertThat(bb.width).isEqualTo(width);
        assertThat(bb.height).isEqualTo(height);
    }

    // ── MovementIntent ──

    @Property
    void movementIntentPreservesTarget(@ForAll double x, @ForAll double y) {
        var intent = new MovementIntent(x, y);

        assertThat(intent.targetX).isEqualTo(x);
        assertThat(intent.targetY).isEqualTo(y);
    }

    // ── SpawnPoint ──

    @Property
    void spawnPointPreservesValues(
            @ForAll double x,
            @ForAll double y,
            @ForAll @DoubleRange(min = 0.0) double leash) {
        var sp = new SpawnPoint(x, y, leash);

        assertThat(sp.x).isEqualTo(x);
        assertThat(sp.y).isEqualTo(y);
        assertThat(sp.leashRadius).isEqualTo(leash);
    }

    // ── Portal ──

    @Property
    void portalPreservesValues(
            @ForAll @StringLength(min = 1, max = 50) String zoneId,
            @ForAll @StringLength(min = 1, max = 50) String portalId) {
        var portal = new Portal(zoneId, portalId);

        assertThat(portal.targetZoneId).isEqualTo(zoneId);
        assertThat(portal.targetPortalId).isEqualTo(portalId);
    }

    // ── PortalCooldown ──

    @Property
    void portalCooldownPreservesTicks(@ForAll @IntRange(min = 0) int ticks) {
        var cd = new PortalCooldown(ticks);

        assertThat(cd.ticksRemaining).isEqualTo(ticks);
    }

    // ── Dirty ──

    @Property
    void dirtyPreservesTick(@ForAll long tick) {
        var dirty = new Dirty(tick);

        assertThat(dirty.tick).isEqualTo(tick);
    }

    // ── Zone ──

    @Property
    void zonePreservesId(@ForAll @StringLength(min = 1, max = 50) String zoneId) {
        var zone = new Zone(zoneId);

        assertThat(zone.zoneId).isEqualTo(zoneId);
    }

    // ── All components implement Component ──

    @Property
    void allComponentsImplementMarkerInterface(@ForAll double x, @ForAll double y) {
        assertThat(new Position(x, y)).isInstanceOf(Component.class);
        assertThat(new Velocity(x, y)).isInstanceOf(Component.class);
        assertThat(new BoundingBox(1.0, 1.0)).isInstanceOf(Component.class);
        assertThat(new Stats(10, 10, 0, 0)).isInstanceOf(Component.class);
        assertThat(new Experience(0, 100)).isInstanceOf(Component.class);
        assertThat(new MovementIntent(x, y)).isInstanceOf(Component.class);
        assertThat(new Dead(-1)).isInstanceOf(Component.class);
        assertThat(new Dirty(0)).isInstanceOf(Component.class);
        assertThat(new Solid()).isInstanceOf(Component.class);
        assertThat(new Portal("z", "p")).isInstanceOf(Component.class);
        assertThat(new PortalCooldown(0)).isInstanceOf(Component.class);
        assertThat(new SpawnPoint(0, 0, 0)).isInstanceOf(Component.class);
        assertThat(new Persistent("acc")).isInstanceOf(Component.class);
        assertThat(new Renderable("n", "v")).isInstanceOf(Component.class);
        assertThat(new Zone("z")).isInstanceOf(Component.class);
    }
}
