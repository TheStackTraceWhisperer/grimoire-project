package com.grimoire.application.core.system;

import com.grimoire.application.core.ecs.ComponentManager;
import com.grimoire.application.core.ecs.EcsWorld;
import com.grimoire.application.core.ecs.EntityManager;
import com.grimoire.domain.core.component.Dirty;
import com.grimoire.domain.core.component.Experience;
import com.grimoire.domain.core.component.Stats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LevelUpSystemTest {

    private EcsWorld world;
    private LevelUpSystem system;

    @BeforeEach
    void setUp() {
        world = new EcsWorld(new EntityManager(), new ComponentManager());
        system = new LevelUpSystem(world);
    }

    @Test
    void noLevelUpWhenBelowThreshold() {
        String entity = world.createEntity();
        world.addComponent(entity, new Experience(50, 100));
        world.addComponent(entity, new Stats(100, 100, 5, 10));

        system.tick(0.05f);

        Experience exp = world.getComponent(entity, Experience.class).orElseThrow();
        assertThat(exp.currentXp()).isEqualTo(50);
        assertThat(exp.xpToNextLevel()).isEqualTo(100);
    }

    @Test
    void levelUpAppliedWhenXpReachesThreshold() {
        String entity = world.createEntity();
        world.addComponent(entity, new Experience(100, 100));
        world.addComponent(entity, new Stats(100, 100, 5, 10));

        system.tick(0.05f);

        Experience exp = world.getComponent(entity, Experience.class).orElseThrow();
        assertThat(exp.currentXp()).isZero();
        assertThat(exp.xpToNextLevel()).isGreaterThan(100);
    }

    @Test
    void statsAreBoostedOnLevelUp() {
        String entity = world.createEntity();
        world.addComponent(entity, new Experience(100, 100));
        world.addComponent(entity, new Stats(100, 100, 5, 10));

        system.tick(0.05f);

        Stats stats = world.getComponent(entity, Stats.class).orElseThrow();
        assertThat(stats.maxHp()).isEqualTo(110);
        assertThat(stats.attack()).isEqualTo(12);
        assertThat(stats.defense()).isEqualTo(6);
    }

    @Test
    void entityMarkedDirtyAfterLevelUp() {
        String entity = world.createEntity();
        world.addComponent(entity, new Experience(100, 100));
        world.addComponent(entity, new Stats(100, 100, 5, 10));

        system.tick(0.05f);

        assertThat(world.hasComponent(entity, Dirty.class)).isTrue();
    }

    @Test
    void notDirtyWhenNoLevelUp() {
        String entity = world.createEntity();
        world.addComponent(entity, new Experience(50, 100));
        world.addComponent(entity, new Stats(100, 100, 5, 10));

        system.tick(0.05f);

        assertThat(world.hasComponent(entity, Dirty.class)).isFalse();
    }

    @Test
    void multipleLevelUpsInOneTick() {
        String entity = world.createEntity();
        // XP=500, threshold=100 → should level up multiple times
        world.addComponent(entity, new Experience(500, 100));
        world.addComponent(entity, new Stats(100, 100, 5, 10));

        system.tick(0.05f);

        Experience exp = world.getComponent(entity, Experience.class).orElseThrow();
        assertThat(exp.currentXp()).isLessThan(exp.xpToNextLevel());
        Stats stats = world.getComponent(entity, Stats.class).orElseThrow();
        assertThat(stats.maxHp()).isGreaterThan(110);
    }

    @Test
    void entityWithoutStatsStillLevelsExperience() {
        String entity = world.createEntity();
        world.addComponent(entity, new Experience(100, 100));
        // No Stats component

        system.tick(0.05f);

        Experience exp = world.getComponent(entity, Experience.class).orElseThrow();
        assertThat(exp.currentXp()).isZero();
        assertThat(exp.xpToNextLevel()).isGreaterThan(100);
    }

    @Test
    void xpRollsOverOnLevelUp() {
        String entity = world.createEntity();
        world.addComponent(entity, new Experience(130, 100));
        world.addComponent(entity, new Stats(100, 100, 5, 10));

        system.tick(0.05f);

        Experience exp = world.getComponent(entity, Experience.class).orElseThrow();
        assertThat(exp.currentXp()).isEqualTo(30);
    }
}
