package com.grimoire.ecs;

import jakarta.inject.Singleton;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages component data for all entities.
 * Maps ComponentType -> EntityId -> ComponentInstance
 */
@Singleton
@SuppressWarnings("PMD.CommentRequired")
public class ComponentManager {
    
    // ComponentType -> (EntityId -> Component)
    private final Map<Class<? extends Component>, Map<String, Component>> components = new ConcurrentHashMap<>();
    
    /**
     * Adds or replaces a component for an entity.
     * @param entityId the entity ID
     * @param component the component
     */
    public void addComponent(String entityId, Component component) {
        components.computeIfAbsent(component.getClass(), k -> new ConcurrentHashMap<>())
                .put(entityId, component);
    }
    
    /**
     * Gets a component for an entity.
     * @param entityId the entity ID
     * @param componentClass the component class
     * @return Optional containing the component if present
     */
    public <T extends Component> Optional<T> getComponent(String entityId, Class<T> componentClass) {
        Map<String, Component> entityComponents = components.get(componentClass);
        if (entityComponents == null) {
            return Optional.empty();
        }
        @SuppressWarnings("unchecked")
        T component = (T) entityComponents.get(entityId);
        return Optional.ofNullable(component);
    }
    
    /**
     * Checks if an entity has a component.
     * @param entityId the entity ID
     * @param componentClass the component class
     * @return true if the entity has the component
     */
    public boolean hasComponent(String entityId, Class<? extends Component> componentClass) {
        Map<String, Component> entityComponents = components.get(componentClass);
        return entityComponents != null && entityComponents.containsKey(entityId);
    }
    
    /**
     * Removes a component from an entity.
     * @param entityId the entity ID
     * @param componentClass the component class
     */
    public void removeComponent(String entityId, Class<? extends Component> componentClass) {
        Map<String, Component> entityComponents = components.get(componentClass);
        if (entityComponents != null) {
            entityComponents.remove(entityId);
        }
    }
    
    /**
     * Removes all components for an entity.
     * @param entityId the entity ID
     */
    public void removeAllComponents(String entityId) {
        for (Map<String, Component> entityComponents : components.values()) {
            entityComponents.remove(entityId);
        }
    }
    
    /**
     * Gets all entity IDs that have a specific component.
     * @param componentClass the component class
     * @return iterable of entity IDs
     */
    public Iterable<String> getEntitiesWithComponent(Class<? extends Component> componentClass) {
        Map<String, Component> entityComponents = components.get(componentClass);
        if (entityComponents == null) {
            return java.util.Collections.emptyList();
        }
        return entityComponents.keySet();
    }
    
    /**
     * Gets all components for an entity.
     * @param entityId the entity ID
     * @return map of component class to component instance
     */
    public Map<Class<? extends Component>, Component> getAllComponents(String entityId) {
        Map<Class<? extends Component>, Component> result = new ConcurrentHashMap<>();
        for (Map.Entry<Class<? extends Component>, Map<String, Component>> entry : components.entrySet()) {
            Component component = entry.getValue().get(entityId);
            if (component != null) {
                result.put(entry.getKey(), component);
            }
        }
        return result;
    }
}
