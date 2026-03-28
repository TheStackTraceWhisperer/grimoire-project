package com.grimoire.ecs;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * The primary game state bean managing all entities, components, and systems.
 */
@Singleton
@SuppressWarnings("PMD.CommentRequired")
public class EcsWorld {
    
    private final EntityManager entityManager;
    private final ComponentManager componentManager;
    private final Map<String, Prefab> prefabs = new ConcurrentHashMap<>();
    private long currentTick;

    @Inject
    @SuppressFBWarnings(
        value = "EI_EXPOSE_REP2",
        justification = "EntityManager and ComponentManager are DI-managed collaborators intentionally shared by reference."
    )
    public EcsWorld(EntityManager entityManager, ComponentManager componentManager) {
        this.entityManager = entityManager;
        this.componentManager = componentManager;
    }
    
    /**
     * Creates a new entity.
     * @return the entity ID
     */
    public String createEntity() {
        return entityManager.createEntity();
    }
    
    /**
     * Destroys an entity and all its components.
     * @param entityId the entity ID
     */
    public void destroyEntity(String entityId) {
        componentManager.removeAllComponents(entityId);
        entityManager.destroyEntity(entityId);
    }
    
    /**
     * Adds a component to an entity.
     * @param entityId the entity ID
     * @param component the component
     */
    public void addComponent(String entityId, Component component) {
        componentManager.addComponent(entityId, component);
    }
    
    /**
     * Gets a component from an entity.
     * @param entityId the entity ID
     * @param componentClass the component class
     * @return Optional containing the component if present
     */
    public <T extends Component> Optional<T> getComponent(String entityId, Class<T> componentClass) {
        return componentManager.getComponent(entityId, componentClass);
    }
    
    /**
     * Checks if an entity has a component.
     * @param entityId the entity ID
     * @param componentClass the component class
     * @return true if the entity has the component
     */
    public boolean hasComponent(String entityId, Class<? extends Component> componentClass) {
        return componentManager.hasComponent(entityId, componentClass);
    }
    
    /**
     * Removes a component from an entity.
     * @param entityId the entity ID
     * @param componentClass the component class
     */
    public void removeComponent(String entityId, Class<? extends Component> componentClass) {
        componentManager.removeComponent(entityId, componentClass);
    }
    
    /**
     * Gets all entity IDs.
     * @return collection of all entity IDs
     */
    public Iterable<String> getAllEntities() {
        return entityManager.getAllEntityIds();
    }
    
    /**
     * Gets all entity IDs that have a specific component.
     * @param componentClass the component class
     * @return iterable of entity IDs
     */
    public Iterable<String> getEntitiesWithComponent(Class<? extends Component> componentClass) {
        return componentManager.getEntitiesWithComponent(componentClass);
    }
    
    /**
     * Gets all components for an entity.
     * @param entityId the entity ID
     * @return map of component class to component instance
     */
    public Map<Class<? extends Component>, Component> getAllComponents(String entityId) {
        return componentManager.getAllComponents(entityId);
    }
    
    /**
     * Gets the current tick count.
     * @return current tick
     */
    public long getCurrentTick() {
        return currentTick;
    }
    
    /**
     * Increments the tick counter.
     */
    public void incrementTick() {
        currentTick++;
    }
    
    /**
     * Checks if an entity exists.
     * @param entityId the entity ID
     * @return true if the entity exists
     */
    public boolean entityExists(String entityId) {
        return entityManager.exists(entityId);
    }
    
    /**
     * Registers a prefab with the ECS world.
     * @param prefab the prefab to register
     */
    public void registerPrefab(Prefab prefab) {
        prefabs.put(prefab.getName(), prefab);
    }
    
    /**
     * Creates an entity from a prefab.
     * @param prefabName the name of the prefab
     * @return the entity ID
     */
    public String createEntityFromPrefab(String prefabName) {
        return createEntityFromPrefab(prefabName, null);
    }
    
    /**
     * Creates an entity from a prefab with optional component customization.
     * @param prefabName the name of the prefab
     * @param componentCustomizer optional function to customize components before adding them
     * @return the entity ID
     */
    public String createEntityFromPrefab(String prefabName, Function<Component, Component> componentCustomizer) {
        Objects.requireNonNull(prefabName, "Prefab name cannot be null");
        Prefab prefab = prefabs.get(prefabName);
        if (prefab == null) {
            throw new IllegalArgumentException("Prefab not found: " + prefabName);
        }
        
        String entityId = createEntity();
        for (Component component : prefab.getComponentTemplates()) {
            Component componentToAdd = componentCustomizer != null
                ? componentCustomizer.apply(component) : component;
            if (componentToAdd != null) {
                addComponent(entityId, componentToAdd);
            }
        }
        return entityId;
    }
}
