package com.grimoire.application.core.system;

import com.grimoire.application.core.ecs.ComponentManager;
import com.grimoire.application.core.ecs.EcsWorld;
import com.grimoire.application.core.ecs.EntityManager;
import com.grimoire.application.core.port.GameConfig;
import com.grimoire.application.core.port.GameEventPort;
import com.grimoire.domain.combat.component.AttackCooldown;
import com.grimoire.domain.combat.component.AttackIntent;
import com.grimoire.domain.combat.component.Monster;
import com.grimoire.domain.core.component.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class CombatSystemTest {

    private EcsWorld world;
    private GameEventPort gameEventPort;
    private SpatialGridSystem spatialGridSystem;
    private CombatSystem system;

    @BeforeEach
    void setUp() {
        world = new EcsWorld(new EntityManager(), new ComponentManager());
        gameEventPort = Mockito.mock(GameEventPort.class);
        GameConfig config = new GameConfig() {
        };
        spatialGridSystem = new SpatialGridSystem(world, config);
        system = new CombatSystem(world, config, gameEventPort, spatialGridSystem);
    }

    @Test
    void attackDealsDamage() {
        int attacker = createEntityWithStats(0, 0, 100, 100, 0, 20);
        int target = createEntityWithStats(5, 0, 50, 50, 5, 10);
        world.addComponent(attacker, new AttackIntent(target));

        system.tick(0L);

        Stats targetStats = world.getComponent(target, Stats.class);
        assertThat(targetStats.hp).isEqualTo(35); // 50 - max(1, 20-5) = 50-15 = 35
    }

    @Test
    void attackMarksTargetDirty() {
        int attacker = createEntityWithStats(0, 0, 100, 100, 0, 20);
        int target = createEntityWithStats(5, 0, 50, 50, 5, 10);
        world.addComponent(attacker, new AttackIntent(target));

        system.tick(0L);

        assertThat(world.hasComponent(target, Dirty.class)).isTrue();
    }

    @Test
    void attackIntentRemovedAfterProcessing() {
        int attacker = createEntityWithStats(0, 0, 100, 100, 0, 20);
        int target = createEntityWithStats(5, 0, 50, 50, 5, 10);
        world.addComponent(attacker, new AttackIntent(target));

        system.tick(0L);

        assertThat(world.hasComponent(attacker, AttackIntent.class)).isFalse();
    }

    @Test
    void attackAppliesCooldown() {
        int attacker = createEntityWithStats(0, 0, 100, 100, 0, 20);
        int target = createEntityWithStats(5, 0, 50, 50, 5, 10);
        world.addComponent(attacker, new AttackIntent(target));

        system.tick(0L);

        assertThat(world.hasComponent(attacker, AttackCooldown.class)).isTrue();
    }

    @Test
    void attackRejectedWhileOnCooldown() {
        int attacker = createEntityWithStats(0, 0, 100, 100, 0, 20);
        int target = createEntityWithStats(5, 0, 50, 50, 5, 10);
        world.addComponent(attacker, new AttackCooldown(10));
        world.addComponent(attacker, new AttackIntent(target));

        system.tick(0L);

        Stats targetStats = world.getComponent(target, Stats.class);
        assertThat(targetStats.hp).isEqualTo(50); // No damage
    }

    @Test
    void attackRejectedWhenOutOfRange() {
        int attacker = createEntityWithStats(0, 0, 100, 100, 0, 20);
        // Place target far away (>50 units default range)
        int target = createEntityWithStats(1000, 1000, 50, 50, 5, 10);
        world.addComponent(attacker, new AttackIntent(target));

        system.tick(0L);

        Stats targetStats = world.getComponent(target, Stats.class);
        assertThat(targetStats.hp).isEqualTo(50);
    }

    @Test
    void cooldownDecrements() {
        int entity = world.createEntity();
        world.addComponent(entity, new AttackCooldown(3));

        system.tick(0L);

        AttackCooldown cd = world.getComponent(entity, AttackCooldown.class);
        assertThat(cd.ticksRemaining).isEqualTo(2);
    }

    @Test
    void cooldownRemovedAtZero() {
        int entity = world.createEntity();
        world.addComponent(entity, new AttackCooldown(1));

        system.tick(0L);

        assertThat(world.hasComponent(entity, AttackCooldown.class)).isFalse();
    }

    @Test
    void targetKilledWhenHpReachesZero() {
        int attacker = createEntityWithStats(0, 0, 100, 100, 0, 100);
        int target = createEntityWithStats(5, 0, 1, 50, 0, 10);
        world.addComponent(attacker, new AttackIntent(target));

        system.tick(0L);

        // Entity is killed AND destroyed in the same tick
        assertThat(world.entityExists(target)).isFalse();
        verify(gameEventPort).onEntityDespawn(eq(target), anyString());
    }

    @Test
    void deadEntityIsDestroyedAndDespawnNotified() {
        int attacker = createEntityWithStats(0, 0, 100, 100, 0, 100);
        int target = createEntityWithStats(5, 0, 1, 50, 0, 10);
        world.addComponent(target, new Zone("zone1"));
        world.addComponent(attacker, new AttackIntent(target));

        system.tick(0L);

        assertThat(world.entityExists(target)).isFalse();
        verify(gameEventPort).onEntityDespawn(eq(target), eq("zone1"));
    }

    @Test
    void xpAwardedFromMonsterKill() {
        int attacker = createEntityWithStats(0, 0, 100, 100, 0, 100);
        world.addComponent(attacker, new Experience(0, 100));
        int monster = createEntityWithStats(5, 0, 1, 50, 0, 10);
        world.addComponent(monster, new Monster(Monster.MonsterType.RAT, 25));
        world.addComponent(attacker, new AttackIntent(monster));

        system.tick(0L);

        Experience exp = world.getComponent(attacker, Experience.class);
        assertThat(exp.currentXp).isEqualTo(25);
    }

    @Test
    void attackOnNonExistentTargetIsIgnored() {
        int attacker = createEntityWithStats(0, 0, 100, 100, 0, 20);
        world.addComponent(attacker, new AttackIntent(99999));

        system.tick(0L);

        verify(gameEventPort, never()).onEntityDespawn(anyInt(), anyString());
    }

    @Test
    void attackOnAlreadyDeadTargetIsIgnored() {
        int attacker = createEntityWithStats(0, 0, 100, 100, 0, 20);
        int target = createEntityWithStats(5, 0, 50, 50, 5, 10);
        world.addComponent(target, new Dead(-1));
        world.addComponent(attacker, new AttackIntent(target));

        system.tick(0L);

        // Attack was skipped — attacker should NOT get a cooldown
        assertThat(world.hasComponent(attacker, AttackCooldown.class)).isFalse();
    }

    @Test
    void minimumDamageIsOne() {
        // Target has very high defense
        int attacker = createEntityWithStats(0, 0, 100, 100, 0, 1);
        int target = createEntityWithStats(5, 0, 50, 50, 100, 10);
        world.addComponent(attacker, new AttackIntent(target));

        system.tick(0L);

        Stats targetStats = world.getComponent(target, Stats.class);
        assertThat(targetStats.hp).isEqualTo(49);
    }

    private int createEntityWithStats(double x, double y, int hp, int maxHp,
            int defense, int attack) {
        int entity = world.createEntity();
        world.addComponent(entity, new Position(x, y));
        world.addComponent(entity, new Stats(hp, maxHp, defense, attack));
        return entity;
    }
}
