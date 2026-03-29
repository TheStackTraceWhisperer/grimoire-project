package com.grimoire.clientv2.state;

import com.grimoire.shared.component.ComponentDTO;
import com.grimoire.shared.dto.EntityDespawn;
import com.grimoire.shared.dto.EntitySpawn;
import com.grimoire.shared.dto.GameStateUpdate;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Client-side ECS world - the single source of truth for game state.
 * Ported from grimoire-client for LWJGL-based client.
 */
@Slf4j
public class ClientEcsWorld {
    
    private final Map<String, Map<Class<? extends ComponentDTO>, ComponentDTO>> entities = 
            new ConcurrentHashMap<>();
    private final Map<String, ClientPrefab> prefabs = new ConcurrentHashMap<>();
    
    @Getter @Setter
    private String localPlayerEntityId;
    
    @Getter @Setter
    private String currentZone;
    
    private BiConsumer<String, EntitySpawn> entitySpawnListener;
    private BiConsumer<String, EntityDespawn> entityDespawnListener;
    
    /**
     * Gets all entities.
     */
    public Map<String, Map<Class<? extends ComponentDTO>, ComponentDTO>> getEntities() {
        return entities;
    }
    
    /**
     * Gets a specific entity's components.
     */
    public Map<Class<? extends ComponentDTO>, ComponentDTO> getEntity(String entityId) {
        return entities.get(entityId);
    }
    
    /**
     * Gets a specific component from an entity.
     */
    @SuppressWarnings("unchecked")
    public <T extends ComponentDTO> T getComponent(String entityId, Class<T> componentClass) {
        Map<Class<? extends ComponentDTO>, ComponentDTO> entityComponents = entities.get(entityId);
        if (entityComponents == null) return null;
        return (T) entityComponents.get(componentClass);
    }
    
    /**
     * Clears all entities from the world (used on zone changes).
     */
    public void clearAllEntities() {
        entities.clear();
        log.debug("Cleared all entities");
    }
    
    /**
     * Processes a game state update by merging deltas.
     */
    public void processStateUpdate(GameStateUpdate update) {
        for (Map.Entry<String, List<ComponentDTO>> entry : update.entityUpdates().entrySet()) {
            String entityId = entry.getKey();
            List<ComponentDTO> components = entry.getValue();
            
            // Get or create entity component map
            Map<Class<? extends ComponentDTO>, ComponentDTO> entityComponents = 
                    entities.computeIfAbsent(entityId, k -> new ConcurrentHashMap<>());
            
            // Update components
            for (ComponentDTO component : components) {
                entityComponents.put(component.getClass(), component);
            }
        }
    }
    
    /**
     * Spawns a new entity with all its components.
     */
    public void spawnEntity(EntitySpawn spawn) {
        Map<Class<? extends ComponentDTO>, ComponentDTO> components = new ConcurrentHashMap<>();
        for (ComponentDTO component : spawn.allComponents()) {
            components.put(component.getClass(), component);
        }
        entities.put(spawn.entityId(), components);
        log.debug("Spawned entity: {}", spawn.entityId());
        
        if (entitySpawnListener != null) {
            entitySpawnListener.accept(spawn.entityId(), spawn);
        }
    }
    
    /**
     * Despawns an entity.
     */
    public void despawnEntity(EntityDespawn despawn) {
        entities.remove(despawn.entityId());
        log.debug("Despawned entity: {}", despawn.entityId());
        
        if (entityDespawnListener != null) {
            entityDespawnListener.accept(despawn.entityId(), despawn);
        }
    }
    
    /**
     * Registers a prefab with the client ECS world.
     */
    public void registerPrefab(ClientPrefab prefab) {
        prefabs.put(prefab.getName(), prefab);
    }
    
    /**
     * Creates an entity from a prefab.
     */
    public String createEntityFromPrefab(String prefabName, String entityId) {
        return createEntityFromPrefab(prefabName, entityId, null);
    }
    
    /**
     * Creates an entity from a prefab with optional component customization.
     */
    public String createEntityFromPrefab(String prefabName, String entityId, 
                                          Function<ComponentDTO, ComponentDTO> componentCustomizer) {
        if (prefabName == null) throw new IllegalArgumentException("Prefab name cannot be null");
        if (entityId == null) throw new IllegalArgumentException("Entity ID cannot be null");
        
        ClientPrefab prefab = prefabs.get(prefabName);
        if (prefab == null) {
            throw new IllegalArgumentException("Prefab not found: " + prefabName);
        }
        
        Map<Class<? extends ComponentDTO>, ComponentDTO> components = new ConcurrentHashMap<>();
        for (ComponentDTO component : prefab.getComponentTemplates()) {
            ComponentDTO componentToAdd = componentCustomizer != null ? 
                componentCustomizer.apply(component) : component;
            if (componentToAdd != null) {
                components.put(componentToAdd.getClass(), componentToAdd);
            }
        }
        entities.put(entityId, components);
        return entityId;
    }
    
    /**
     * Sets a listener for entity spawn events.
     */
    public void setEntitySpawnListener(BiConsumer<String, EntitySpawn> listener) {
        this.entitySpawnListener = listener;
    }
    
    /**
     * Sets a listener for entity despawn events.
     */
    public void setEntityDespawnListener(BiConsumer<String, EntityDespawn> listener) {
        this.entityDespawnListener = listener;
    }
    
    /**
     * Gets the number of entities.
     */
    public int getEntityCount() {
        return entities.size();
    }
}
