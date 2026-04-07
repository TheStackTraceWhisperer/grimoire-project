package com.grimoire.application.core.system;

import com.grimoire.application.core.ecs.ComponentManager;
import com.grimoire.application.core.ecs.EcsWorld;
import com.grimoire.application.core.ecs.EntityManager;
import com.grimoire.application.core.port.GameConfig;
import com.grimoire.domain.combat.component.AttackIntent;
import com.grimoire.domain.combat.component.NpcAi;
import com.grimoire.domain.core.component.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

class NpcAiSystemTest {

    private EcsWorld world;
    private SpatialGridSystem spatialGridSystem;
    private NpcAiSystem system;

    @BeforeEach
    void setUp() {
        world = new EcsWorld(new EntityManager(), new ComponentManager());
        GameConfig config = new GameConfig() {
            @Override
            public double npcAggroRange() {
                return 100.0;
            }

            @Override
            public double attackRange() {
                return 5.0;
            }

            @Override
            public double npcLeashRadius() {
                return 50.0;
            }
        };
        spatialGridSystem = new SpatialGridSystem(world, config);
        // Seeded random for deterministic tests
        system = new NpcAiSystem(world, config, spatialGridSystem, new Random(42));
    }

    @Test
    void friendlyWanderSetsVelocity() {
        int npc = world.createEntity();
        world.addComponent(npc, new NpcAi(NpcAi.AiType.FRIENDLY_WANDER));
        world.addComponent(npc, new Position(0, 0));

        // Tick many times to hit the 5% wander chance
        for (int i = 0; i < 100; i++) {
            system.tick(0L);
        }

        assertThat(world.hasComponent(npc, Velocity.class)).isTrue();
    }

    @Test
    void hostileNpcChasesNearbyPlayer() {
        int npc = createNpc(0, 0, NpcAi.AiType.HOSTILE_AGGRO_MELEE);
        int player = createPlayer(30, 0);

        // Rebuild spatial grid so player appears nearby
        rebuildGrid();

        system.tick(0L);

        Velocity vel = world.getComponent(npc, Velocity.class);
        assertThat(vel.dx).isGreaterThan(0); // Moving toward player
    }

    @Test
    void hostileNpcAttacksWhenInRange() {
        int npc = createNpc(0, 0, NpcAi.AiType.HOSTILE_AGGRO_MELEE);
        int player = createPlayer(2, 0); // Within 5-unit attack range

        rebuildGrid();

        system.tick(0L);

        assertThat(world.hasComponent(npc, AttackIntent.class)).isTrue();
        AttackIntent intent = world.getComponent(npc, AttackIntent.class);
        assertThat(intent.targetEntityId).isEqualTo(player);
    }

    @Test
    void hostileNpcStopsWhenNoPlayersNearby() {
        int npc = createNpc(0, 0, NpcAi.AiType.HOSTILE_AGGRO_MELEE);
        // No players anywhere

        system.tick(0L);

        Velocity vel = world.getComponent(npc, Velocity.class);
        assertThat(vel.dx).isZero();
        assertThat(vel.dy).isZero();
    }

    @Test
    void hostileNpcReturnsToSpawnWhenOutOfLeash() {
        // NPC at (100, 0) with spawn at (0, 0) and leash radius 50
        int npc = world.createEntity();
        world.addComponent(npc, new NpcAi(NpcAi.AiType.HOSTILE_AGGRO_MELEE));
        world.addComponent(npc, new Position(100, 0));
        world.addComponent(npc, new Zone("zone1"));
        world.addComponent(npc, new Solid());
        world.addComponent(npc, new SpawnPoint(0, 0, 50));

        // Player near the NPC but beyond leash
        int player = createPlayer(105, 0);
        rebuildGrid();
        spatialGridSystem.getGrid().updateEntity(npc, 100, 0, "zone1");

        system.tick(0L);

        Velocity vel = world.getComponent(npc, Velocity.class);
        assertThat(vel.dx).isLessThan(0); // Moving back toward spawn (0,0)
    }

    @Test
    void deadNpcIsSkipped() {
        int npc = createNpc(0, 0, NpcAi.AiType.HOSTILE_AGGRO_MELEE);
        world.addComponent(npc, new Dead(-1));
        int player = createPlayer(10, 0);
        rebuildGrid();

        system.tick(0L);

        assertThat(world.hasComponent(npc, AttackIntent.class)).isFalse();
    }

    @Test
    void npcIgnoresDeadPlayers() {
        int npc = createNpc(0, 0, NpcAi.AiType.HOSTILE_AGGRO_MELEE);
        int player = createPlayer(10, 0);
        world.addComponent(player, new Dead(-1));
        rebuildGrid();

        system.tick(0L);

        Velocity vel = world.getComponent(npc, Velocity.class);
        assertThat(vel.dx).isZero();
    }

    @Test
    void npcChasesClosestPlayer() {
        int npc = createNpc(0, 0, NpcAi.AiType.HOSTILE_AGGRO_MELEE);
        int farPlayer = createPlayer(50, 0);
        int nearPlayer = createPlayer(10, 0);

        spatialGridSystem.getGrid().updateEntity(farPlayer, 50, 0, "zone1");
        spatialGridSystem.getGrid().updateEntity(nearPlayer, 10, 0, "zone1");

        system.tick(0L);

        // Should chase nearest
        Velocity vel = world.getComponent(npc, Velocity.class);
        assertThat(vel.dx).isGreaterThan(0);
    }

    @Test
    void npcWithoutPositionIsSkipped() {
        int npc = world.createEntity();
        world.addComponent(npc, new NpcAi(NpcAi.AiType.HOSTILE_AGGRO_MELEE));
        world.addComponent(npc, new Zone("zone1"));
        // No Position

        system.tick(0L);

        assertThat(world.hasComponent(npc, Velocity.class)).isFalse();
    }

    private int createNpc(double x, double y, NpcAi.AiType type) {
        int npc = world.createEntity();
        world.addComponent(npc, new NpcAi(type));
        world.addComponent(npc, new Position(x, y));
        world.addComponent(npc, new Zone("zone1"));
        world.addComponent(npc, new Solid());
        return npc;
    }

    private int createPlayer(double x, double y) {
        int player = world.createEntity();
        world.addComponent(player, new PlayerControlled("session-1"));
        world.addComponent(player, new Position(x, y));
        world.addComponent(player, new Zone("zone1"));
        world.addComponent(player, new Solid());
        return player;
    }

    private void rebuildGrid() {
        spatialGridSystem.tick(0L);
    }
}
