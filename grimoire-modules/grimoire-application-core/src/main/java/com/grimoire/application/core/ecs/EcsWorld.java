package com.grimoire.application.core.ecs;

import com.grimoire.domain.core.component.Component;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * The primary game state bean managing all entities, components, and prefabs.
 *
 * <p>
 * Entities are primitive ints. Delegates entity lifecycle to
 * {@link EntityManager} and component storage to {@link ComponentManager}.
 * </p>
 *
 * <p>
 * <strong>Thread-safety:</strong> All entity/component operations must be
 * called from the single-threaded game loop. Only prefab registration is
 * thread-safe. Register as a singleton at the assembly layer.
 * </p>
 */
public class EcsWorld {

    /**
     * Manages entity lifecycle (create/destroy/exists).
     */
    private final EntityManager entityManager;

    /**
     * Manages component storage and queries.
     */
    private final ComponentManager componentManager;

    /**
     * Registered prefab templates, keyed by name. Thread-safe for startup
     * registration.
     */
    private final Map<String, Prefab> prefabs = new ConcurrentHashMap<>();

    /**
     * Monotonically increasing tick counter.
     */
    private long currentTick;

    /**
     * Creates an EcsWorld with the given managers.
     *
     * @param entityManager
     *            the entity manager
     * @param componentManager
     *            the component manager
     */
    public EcsWorld(EntityManager entityManager, ComponentManager componentManager) {
        this.entityManager = entityManager;
        this.componentManager = componentManager;
    }

    /**
     * Creates a new entity.
     *
     * @return the entity ID (primitive int)
     */
    public int createEntity() {
        return entityManager.createEntity();
    }

    /**
     * Destroys an entity and all its components.
     *
     * @param entityId
     *            the entity ID
     */
    public void destroyEntity(int entityId) {
        componentManager.removeAllComponents(entityId);
        entityManager.destroyEntity(entityId);
    }

    /**
     * Adds a component to an entity.
     *
     * @param entityId
     *            the entity ID
     * @param component
     *            the component
     */
    public void addComponent(int entityId, Component component) {
        componentManager.addComponent(entityId, component);
    }

    /**
     * Gets a component from an entity.
     *
     * @param entityId
     *            the entity ID
     * @param componentClass
     *            the component class
     * @param <T>
     *            the component type
     * @return the component, or null if absent
     */
    public <T extends Component> T getComponent(int entityId, Class<T> componentClass) {
        return componentManager.getComponent(entityId, componentClass);
    }

    /**
     * Checks if an entity has a component.
     *
     * @param entityId
     *            the entity ID
     * @param componentClass
     *            the component class
     * @return true if the entity has the component
     */
    public boolean hasComponent(int entityId, Class<? extends Component> componentClass) {
        return componentManager.hasComponent(entityId, componentClass);
    }

    /**
     * Removes a component from an entity.
     *
     * @param entityId
     *            the entity ID
     * @param componentClass
     *            the component class
     */
    public void removeComponent(int entityId, Class<? extends Component> componentClass) {
        componentManager.removeComponent(entityId, componentClass);
    }

    /**
     * Returns the high-water mark for entity iteration.
     *
     * @return the exclusive upper bound of entity IDs ever created
     */
    public int getMaxEntityId() {
        return entityManager.getMaxEntityId();
    }

    /**
     * Returns the alive array for direct system iteration.
     *
     * @return the alive flags indexed by entity ID
     */
    public boolean[] getAlive() {
        return entityManager.getAlive();
    }

    /**
     * Returns the dense array of active entity IDs for cache-friendly iteration.
     *
     * <p>
     * Only indices {@code [0, getActiveCount())} contain valid IDs.
     * </p>
     *
     * @return the active entity ID array
     */
    public int[] getActiveEntities() {
        return entityManager.getActiveEntities();
    }

    /**
     * Returns the number of currently active entities.
     *
     * @return active entity count
     */
    public int getActiveCount() {
        return entityManager.getActiveCount();
    }

    /**
     * Gets all components for an entity.
     *
     * @param entityId
     *            the entity ID
     * @return map of component class to component instance
     */
    public Map<Class<? extends Component>, Component> getAllComponents(int entityId) {
        return componentManager.getAllComponents(entityId);
    }

    /**
     * Returns the underlying ComponentManager for direct array access by systems.
     *
     * @return the component manager
     */
    public ComponentManager getComponentManager() {
        return componentManager;
    }

    /**
     * Gets the current tick count.
     *
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
     *
     * @param entityId
     *            the entity ID
     * @return true if the entity exists
     */
    public boolean entityExists(int entityId) {
        return entityManager.exists(entityId);
    }

    /**
     * Registers a prefab with the ECS world.
     *
     * @param prefab
     *            the prefab to register
     */
    public void registerPrefab(Prefab prefab) {
        prefabs.put(prefab.getName(), prefab);
    }

    /**
     * Creates an entity from a prefab.
     *
     * @param prefabName
     *            the name of the prefab
     * @return the entity ID
     */
    public int createEntityFromPrefab(String prefabName) {
        return createEntityFromPrefab(prefabName, null);
    }

    /**
     * Creates an entity from a prefab with optional component customization.
     *
     * @param prefabName
     *            the name of the prefab
     * @param componentCustomizer
     *            optional function to customize components
     * @return the entity ID
     */
    public int createEntityFromPrefab(String prefabName, Function<Component, Component> componentCustomizer) {
        Objects.requireNonNull(prefabName, "Prefab name cannot be null");
        Prefab prefab = prefabs.get(prefabName);
        if (prefab == null) {
            throw new IllegalArgumentException("Prefab not found: " + prefabName);
        }

        int entityId = createEntity();
        for (Component component : prefab.getComponentTemplates()) {
            Component toAdd = componentCustomizer != null ? componentCustomizer.apply(component) : component;
            if (toAdd != null) {
                addComponent(entityId, toAdd);
            }
        }
        return entityId;
    }
}
