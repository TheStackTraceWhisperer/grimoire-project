package com.grimoire.client.state;

import com.grimoire.shared.component.ComponentDTO;
import com.grimoire.shared.dto.EntityDespawn;
import com.grimoire.shared.dto.EntitySpawn;
import com.grimoire.shared.dto.GameStateUpdate;
import jakarta.inject.Singleton;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Client-side ECS world - the single source of truth for UI state.
 */
@Singleton
public class ClientEcsWorld {
    
    private final ObservableMap<String, Map<Class<? extends ComponentDTO>, ComponentDTO>> entities = 
            FXCollections.observableHashMap();
    private final Map<String, ClientPrefab> prefabs = new ConcurrentHashMap<>();
    
    private String localPlayerEntityId;
    private String currentZone;
    
    /**
     * Gets the observable entities map for UI binding.
     */
    public ObservableMap<String, Map<Class<? extends ComponentDTO>, ComponentDTO>> getEntities() {
        return entities;
    }
    
    /**
     * Clears all entities from the world (used on zone changes).
     */
    public void clearAllEntities() {
        entities.clear();
    }
    
    /**
     * Processes a game state update by merging deltas.
     */
    public void processStateUpdate(GameStateUpdate update) {
        for (Map.Entry<String, java.util.List<ComponentDTO>> entry : update.entityUpdates().entrySet()) {
            String entityId = entry.getKey();
            java.util.List<ComponentDTO> components = entry.getValue();
            
            // Get or create entity component map
            Map<Class<? extends ComponentDTO>, ComponentDTO> entityComponents = 
                    entities.computeIfAbsent(entityId, k -> new HashMap<>());
            
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
        Map<Class<? extends ComponentDTO>, ComponentDTO> components = new HashMap<>();
        for (ComponentDTO component : spawn.allComponents()) {
            components.put(component.getClass(), component);
        }
        entities.put(spawn.entityId(), components);
    }
    
    /**
     * Despawns an entity.
     */
    public void despawnEntity(EntityDespawn despawn) {
        entities.remove(despawn.entityId());
    }
    
    /**
     * Sets the local player entity ID.
     */
    public void setLocalPlayerEntityId(String entityId) {
        this.localPlayerEntityId = entityId;
    }
    
    /**
     * Gets the local player entity ID.
     */
    public String getLocalPlayerEntityId() {
        return localPlayerEntityId;
    }
    
    /**
     * Sets the current zone ID.
     */
    public void setCurrentZone(String zoneId) {
        this.currentZone = zoneId;
    }
    
    /**
     * Gets the current zone ID.
     */
    public String getCurrentZone() {
        return currentZone;
    }
    
    /**
     * Registers a prefab with the client ECS world.
     * @param prefab the prefab to register
     */
    public void registerPrefab(ClientPrefab prefab) {
        prefabs.put(prefab.getName(), prefab);
    }
    
    /**
     * Creates an entity from a prefab.
     * @param prefabName the name of the prefab
     * @param entityId the entity ID to use
     * @return the entity ID
     */
    public String createEntityFromPrefab(String prefabName, String entityId) {
        return createEntityFromPrefab(prefabName, entityId, null);
    }
    
    /**
     * Creates an entity from a prefab with optional component customization.
     * @param prefabName the name of the prefab
     * @param entityId the entity ID to use
     * @param componentCustomizer optional function to customize components before adding them
     * @return the entity ID
     */
    public String createEntityFromPrefab(String prefabName, String entityId, Function<ComponentDTO, ComponentDTO> componentCustomizer) {
        Objects.requireNonNull(prefabName, "Prefab name cannot be null");
        Objects.requireNonNull(entityId, "Entity ID cannot be null");
        ClientPrefab prefab = prefabs.get(prefabName);
        if (prefab == null) {
            throw new IllegalArgumentException("Prefab not found: " + prefabName);
        }
        
        Map<Class<? extends ComponentDTO>, ComponentDTO> components = new HashMap<>();
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
}
