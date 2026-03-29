package engine.services.world;

import java.util.HashMap;
import java.util.Map;

class ComponentManager implements IComponentManager {
  private final Map<Class<?>, Map<Integer, Object>> componentStores = new HashMap<>();

  @Override
  public <T> void addComponent(int entityId, T component) {
    Class<?> componentClass = component.getClass();
    componentStores
      .computeIfAbsent(componentClass, k -> new HashMap<>())
      .put(entityId, component);
  }

  public <T> T getComponent(int entityId, Class<T> componentClass) {
    Map<Integer, Object> store = componentStores.get(componentClass);
    if (store == null) {
      return null;
    }
    return componentClass.cast(store.get(entityId));
  }

  public void removeComponent(int entityId, Class<?> componentClass) {
    Map<Integer, Object> store = componentStores.get(componentClass);
    if (store != null) {
      store.remove(entityId);
    }
  }

  public boolean hasComponent(int entityId, Class<?> componentClass) {
    Map<Integer, Object> store = componentStores.get(componentClass);
    return store != null && store.containsKey(entityId);
  }

  public void entityDestroyed(int entityId) {
    for (Map<Integer, Object> store : componentStores.values()) {
      store.remove(entityId);
    }
  }

}
