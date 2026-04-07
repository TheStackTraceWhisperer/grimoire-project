package com.grimoire.application.core.system;

import com.grimoire.application.core.ecs.ComponentManager;
import com.grimoire.application.core.ecs.EcsWorld;
import com.grimoire.application.core.ecs.EntityManager;
import com.grimoire.domain.core.component.PortalCooldown;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PortalCooldownSystemTest {

    private EcsWorld world;
    private PortalCooldownSystem system;

    @BeforeEach
    void setUp() {
        world = new EcsWorld(new EntityManager(), new ComponentManager());
        system = new PortalCooldownSystem(world);
    }

    @Test
    void tickDecrementsCooldown() {
        int entity = world.createEntity();
        world.addComponent(entity, new PortalCooldown(5));

        system.tick(0L);

        PortalCooldown cooldown = world.getComponent(entity, PortalCooldown.class);
        assertThat(cooldown.ticksRemaining).isEqualTo(4);
    }

    @Test
    void cooldownRemovedWhenReachesZero() {
        int entity = world.createEntity();
        world.addComponent(entity, new PortalCooldown(1));

        system.tick(0L);

        assertThat(world.hasComponent(entity, PortalCooldown.class)).isFalse();
    }

    @Test
    void multipleTicksCountDown() {
        int entity = world.createEntity();
        world.addComponent(entity, new PortalCooldown(3));

        system.tick(0L);
        system.tick(0L);

        PortalCooldown cooldown = world.getComponent(entity, PortalCooldown.class);
        assertThat(cooldown.ticksRemaining).isEqualTo(1);
    }

    @Test
    void multipleEntitiesProcessed() {
        int e1 = world.createEntity();
        int e2 = world.createEntity();
        world.addComponent(e1, new PortalCooldown(2));
        world.addComponent(e2, new PortalCooldown(1));

        system.tick(0L);

        assertThat(world.hasComponent(e1, PortalCooldown.class)).isTrue();
        assertThat(world.hasComponent(e2, PortalCooldown.class)).isFalse();
    }

    @Test
    void entityWithoutCooldownIsUnaffected() {
        int entity = world.createEntity();
        // No PortalCooldown component

        system.tick(0L);

        assertThat(world.hasComponent(entity, PortalCooldown.class)).isFalse();
    }
}
