package com.grimoire.server.content;

import com.grimoire.server.component.*;
import com.grimoire.ecs.EcsWorld;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;

/**
 * Factory for creating NPCs and monsters.
 */
@Singleton
@RequiredArgsConstructor
public class NpcFactory {
    
    private final EcsWorld ecsWorld;
    
    /**
     * Creates a rat monster.
     */
    public String createRat(String zoneId, double x, double y) {
        return createMonsterFromPrefab("RAT", zoneId, x, y);
    }
    
    /**
     * Creates a wolf monster.
     */
    public String createWolf(String zoneId, double x, double y) {
        return createMonsterFromPrefab("WOLF", zoneId, x, y);
    }
    
    /**
     * Creates a bat monster.
     */
    public String createBat(String zoneId, double x, double y) {
        return createMonsterFromPrefab("BAT", zoneId, x, y);
    }
    
    /**
     * Creates a skeleton monster.
     */
    public String createSkeleton(String zoneId, double x, double y) {
        return createMonsterFromPrefab("SKELETON", zoneId, x, y);
    }
    
    /**
     * Helper method to create a monster from a prefab with zone and position.
     */
    private String createMonsterFromPrefab(String prefabName, String zoneId, double x, double y) {
        String entityId = ecsWorld.createEntityFromPrefab(prefabName, component -> {
            // Replace Position component with the specified x, y
            if (component instanceof Position) {
                return new Position(x, y);
            }
            return component;
        });
        ecsWorld.addComponent(entityId, new Zone(zoneId));
        return entityId;
    }
    
    /**
     * Creates a friendly NPC.
     */
    public String createFriendlyNpc(String zoneId, double x, double y, String name) {
        String entityId = ecsWorld.createEntity();
        ecsWorld.addComponent(entityId, new Zone(zoneId));
        ecsWorld.addComponent(entityId, new Position(x, y));
        ecsWorld.addComponent(entityId, new Velocity(0, 0));
        ecsWorld.addComponent(entityId, new Renderable(name, "visual-npc-friendly"));
        ecsWorld.addComponent(entityId, new Stats(100, 100, 10, 5));
        ecsWorld.addComponent(entityId, new NpcAi(NpcAi.AiType.FRIENDLY_WANDER));
        ecsWorld.addComponent(entityId, new BoundingBox(10, 10));
        return entityId;
    }
    
    /**
     * Creates a portal.
     */
    public String createPortal(String zoneId, double x, double y, String portalId,
                               String targetZoneId, String targetPortalId) {
        String entityId = ecsWorld.createEntity();
        ecsWorld.addComponent(entityId, new Zone(zoneId));
        ecsWorld.addComponent(entityId, new Position(x, y));
        ecsWorld.addComponent(entityId, new Renderable(portalId, "visual-portal"));
        ecsWorld.addComponent(entityId, new BoundingBox(20, 20));
        ecsWorld.addComponent(entityId, new Portal(targetZoneId, targetPortalId));
        return entityId;
    }
}
