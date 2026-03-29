package com.grimoire.application.core.ecs;

import com.grimoire.domain.core.component.Component;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * The primary game state bean managing all entities, components, and prefabs.
 *
 * <p>
 * Delegates entity lifecycle to {@link EntityManager} and component storage to
 * {@link ComponentManager}. Prefab registration uses a
 * {@link ConcurrentHashMap} since prefabs may be registered from any thread
 * during startup.
 * </p>
 *
 * <p>
 * <strong>Thread-safety:</strong> All entity/component operations must be
 * called from the single-threaded game loop. Only prefab registration is
 * thread-safe. Register as a singleton at the assembly layer.
 * </p>
 */
public class EcsWorld {

    /** Manages entity lifecycle (create/destroy/exists). */
    private final EntityManager entityManager;

    /** Manages component storage and queries. */
    private final ComponentManager componentManager;

    /**
     * Registered prefab templates, keyed by name. Thread-safe for startup
     * registration.
     */
    private final Map<String, Prefab> prefabs = new ConcurrentHashMap<>();

    /** Monotonically increasing tick counter. */
    private long currentTick;

    /**
     * Creates an EcsWorld with the given managers.
     *
     * @param entityManager
     *            the entity manager
     * @param componentManager
     *            the component manager
     */
    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "EntityManager and ComponentManager are managed collaborators, not external mutable data")
    public EcsWorld(EntityManager entityManager, ComponentManager componentManager) {
        this.entityManager = entityManager;
        this.componentManager = componentManager;
    }

    /**
     * Creates a new entity.
     *
     * @return the entity ID
     */
    public String createEntity() {
        return entityManager.createEntity();
    }

    /**
     * Destroys an entity and all its components.
     *
     * @param entityId
     *            the entity ID
     */
    public void destroyEntity(String entityId) {
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
    public void addComponent(String entityId, Component component) {
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
     * @return Optional containing the component if present
     */
    public <T extends Component> Optional<T> getComponent(String entityId, Class<T> componentClass) {
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
    public boolean hasComponent(String entityId, Class<? extends Component> componentClass) {
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
    public void removeComponent(String entityId, Class<? extends Component> componentClass) {
        componentManager.removeComponent(entityId, componentClass);
    }

    /**
     * Gets all entity IDs.
     *
     * @return iterable of all entity IDs
     */
    public Iterable<String> getAllEntities() {
        return entityManager.getAllEntityIds();
    }

    /**
     * Gets all entity IDs that have a specific component.
     *
     * @param componentClass
     *            the component class
     * @return iterable of entity IDs
     */
    public Iterable<String> getEntitiesWithComponent(Class<? extends Component> componentClass) {
        return componentManager.getEntitiesWithComponent(componentClass);
    }

    /**
     * Gets all components for an entity.
     *
     * @param entityId
     *            the entity ID
     * @return map of component class to component instance
     */
    public Map<Class<? extends Component>, Component> getAllComponents(String entityId) {
        return componentManager.getAllComponents(entityId);
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
    public boolean entityExists(String entityId) {
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
    public String createEntityFromPrefab(String prefabName) {
        return createEntityFromPrefab(prefabName, null);
    }

    /**
     * Creates an entity from a prefab with optional component customization.
     *
     * @param prefabName
     *            the name of the prefab
     * @param componentCustomizer
     *            optional function to customize components before adding them
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
            Component toAdd = componentCustomizer != null ? componentCustomizer.apply(component) : component;
            if (toAdd != null) {
                addComponent(entityId, toAdd);
            }
        }
        return entityId;
    }
}
