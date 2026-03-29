package com.grimoire.application.core.ecs;

import com.grimoire.domain.core.component.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Manages component data for all entities.
 *
 * <p>
 * Stores components in a {@code ComponentType → (EntityId → Component)} map.
 * </p>
 *
 * <p>
 * <strong>Thread-safety:</strong> This class must only be accessed from the
 * single-threaded game loop. Register as a singleton at the assembly layer.
 * </p>
 */
public class ComponentManager {

    /**
     * ComponentType → (EntityId → Component). Single-threaded access only — no
     * concurrent map needed.
     */
    @SuppressWarnings("PMD.UseConcurrentHashMap")
    private final Map<Class<? extends Component>, Map<String, Component>> components = new HashMap<>();

    /**
     * Adds or replaces a component for an entity.
     *
     * @param entityId
     *            the entity ID
     * @param component
     *            the component
     */
    public void addComponent(String entityId, Component component) {
        components.computeIfAbsent(component.getClass(), k -> new HashMap<>())
                .put(entityId, component);
    }

    /**
     * Gets a component for an entity.
     *
     * @param entityId
     *            the entity ID
     * @param componentClass
     *            the component class
     * @param <T>
     *            the component type
     * @return Optional containing the component if present
     */
    @SuppressWarnings("unchecked")
    public <T extends Component> Optional<T> getComponent(String entityId, Class<T> componentClass) {
        Map<String, Component> entityComponents = components.get(componentClass);
        if (entityComponents == null) {
            return Optional.empty();
        }
        return Optional.ofNullable((T) entityComponents.get(entityId));
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
        Map<String, Component> entityComponents = components.get(componentClass);
        return entityComponents != null && entityComponents.containsKey(entityId);
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
        Map<String, Component> entityComponents = components.get(componentClass);
        if (entityComponents != null) {
            entityComponents.remove(entityId);
        }
    }

    /**
     * Removes all components for an entity.
     *
     * @param entityId
     *            the entity ID
     */
    public void removeAllComponents(String entityId) {
        for (Map<String, Component> entityComponents : components.values()) {
            entityComponents.remove(entityId);
        }
    }

    /**
     * Gets all entity IDs that have a specific component.
     *
     * @param componentClass
     *            the component class
     * @return iterable of entity IDs
     */
    public Iterable<String> getEntitiesWithComponent(Class<? extends Component> componentClass) {
        Map<String, Component> entityComponents = components.get(componentClass);
        if (entityComponents == null) {
            return Collections.emptyList();
        }
        return entityComponents.keySet();
    }

    /**
     * Gets all components for an entity.
     *
     * @param entityId
     *            the entity ID
     * @return map of component class to component instance
     */
    public Map<Class<? extends Component>, Component> getAllComponents(String entityId) {
        @SuppressWarnings("PMD.UseConcurrentHashMap")
        Map<Class<? extends Component>, Component> result = new HashMap<>();
        for (Map.Entry<Class<? extends Component>, Map<String, Component>> entry : components.entrySet()) {
            Component component = entry.getValue().get(entityId);
            if (component != null) {
                result.put(entry.getKey(), component);
            }
        }
        return result;
    }
}
