package com.grimoire.server.content;

import com.grimoire.server.component.*;
import com.grimoire.server.ecs.EcsWorld;
import com.grimoire.server.ecs.Prefab;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.context.event.StartupEvent;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

/**
 * Registry that defines and registers all entity prefabs on startup.
 */
@Singleton
@Slf4j
public class PrefabRegistry implements ApplicationEventListener<StartupEvent> {
    
    private final EcsWorld ecsWorld;
    
    @Inject
    public PrefabRegistry(EcsWorld ecsWorld) {
        this.ecsWorld = ecsWorld;
    }
    
    @Override
    public void onApplicationEvent(StartupEvent event) {
        log.info("Registering entity prefabs...");
        registerMonsterPrefabs();
        log.info("Prefab registration complete");
    }
    
    private void registerMonsterPrefabs() {
        // RAT prefab
        Prefab ratPrefab = new Prefab("RAT")
            .addComponent(new Position(0, 0))  // Will be customized when spawning
            .addComponent(new Velocity(0, 0))
            .addComponent(new Renderable("Rat", "visual-monster-rat"))
            .addComponent(new Stats(20, 20, 1, 3))
            .addComponent(new Monster(Monster.MonsterType.RAT))
            .addComponent(new NpcAi(NpcAi.AiType.HOSTILE_AGGRO_MELEE))
            .addComponent(new BoundingBox(8, 8));
        ecsWorld.registerPrefab(ratPrefab);
        
        // WOLF prefab
        Prefab wolfPrefab = new Prefab("WOLF")
            .addComponent(new Position(0, 0))
            .addComponent(new Velocity(0, 0))
            .addComponent(new Renderable("Wolf", "visual-monster-wolf"))
            .addComponent(new Stats(40, 40, 3, 7))
            .addComponent(new Monster(Monster.MonsterType.WOLF))
            .addComponent(new NpcAi(NpcAi.AiType.HOSTILE_AGGRO_MELEE))
            .addComponent(new BoundingBox(10, 10));
        ecsWorld.registerPrefab(wolfPrefab);
        
        // BAT prefab
        Prefab batPrefab = new Prefab("BAT")
            .addComponent(new Position(0, 0))
            .addComponent(new Velocity(0, 0))
            .addComponent(new Renderable("Bat", "visual-monster-bat"))
            .addComponent(new Stats(15, 15, 0, 4))
            .addComponent(new Monster(Monster.MonsterType.BAT))
            .addComponent(new NpcAi(NpcAi.AiType.HOSTILE_AGGRO_MELEE))
            .addComponent(new BoundingBox(6, 6));
        ecsWorld.registerPrefab(batPrefab);
        
        // SKELETON prefab
        Prefab skeletonPrefab = new Prefab("SKELETON")
            .addComponent(new Position(0, 0))
            .addComponent(new Velocity(0, 0))
            .addComponent(new Renderable("Skeleton", "visual-monster-skeleton"))
            .addComponent(new Stats(60, 60, 5, 10))
            .addComponent(new Monster(Monster.MonsterType.SKELETON))
            .addComponent(new NpcAi(NpcAi.AiType.HOSTILE_AGGRO_MELEE))
            .addComponent(new BoundingBox(10, 10));
        ecsWorld.registerPrefab(skeletonPrefab);
    }
}
