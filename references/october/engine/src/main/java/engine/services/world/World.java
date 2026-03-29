package engine.services.world;

import java.util.Set;
import java.util.stream.Collectors;

public class World {
  EntityManager entityManager = new EntityManager();
  ComponentManager componentManager = new ComponentManager();

  public int createEntity() {
    return entityManager.createEntity();
  }

  public void destroyEntity(int entityId) {
    componentManager.entityDestroyed(entityId);
    entityManager.destroyEntity(entityId);
  }

  public <T> void addComponent(int entityId, T component) {
    componentManager.addComponent(entityId, component);
  }

  public <T> T getComponent(int entityId, Class<T> componentClass) {
    return componentManager.getComponent(entityId, componentClass);
  }

  public void removeComponent(int entityId, Class<?> componentClass) {
    componentManager.removeComponent(entityId, componentClass);
  }

  public boolean hasComponent(int entityId, Class<?> componentClass) {
    return componentManager.hasComponent(entityId, componentClass);
  }

  public Set<Integer> getEntitiesWith(Class<?>... componentClasses) {
    // Start with a list of all active entities.
    Set<Integer> activeEntities = entityManager.getActiveEntities();

    if (componentClasses == null || componentClasses.length == 0) {
      return activeEntities;
    }

    // Filter the list down to only those that have all the required components.
    return activeEntities.stream()
      .filter(entityId -> {
        for (Class<?> componentClass : componentClasses) {
          if (!componentManager.hasComponent(entityId, componentClass)) {
            return false; // This entity doesn't have one of the required components.
          }
        }
        return true; // This entity has all required components.
      })
      .collect(Collectors.toSet());
  }
}
