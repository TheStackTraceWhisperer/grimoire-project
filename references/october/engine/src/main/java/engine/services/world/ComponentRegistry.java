package engine.services.world;

import io.micronaut.context.ApplicationContext;
import io.micronaut.core.beans.BeanIntrospection;
import io.micronaut.core.beans.BeanIntrospector;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for component types that auto-discovers all IComponent implementations
 * using Micronaut's bean introspection capabilities. This eliminates the need for
 * manual registration of component classes.
 */
@Slf4j
@Singleton
public class ComponentRegistry {

  private final Map<String, Class<? extends IComponent>> registry;

  @Inject
  public ComponentRegistry(ApplicationContext applicationContext) {
    this.registry = new ConcurrentHashMap<>();
    discoverComponents();
  }

  /**
   * Auto-discovers all classes that implement IComponent and are annotated
   * with @Introspected.
   */
  private void discoverComponents() {
    Collection<BeanIntrospection<Object>> introspections =
      BeanIntrospector.SHARED.findIntrospections(ref -> {
        Class<?> beanType = ref.getBeanType();
        return IComponent.class.isAssignableFrom(beanType);
      });

    for (BeanIntrospection<Object> introspection : introspections) {
      Class<?> beanType = introspection.getBeanType();
      if (IComponent.class.isAssignableFrom(beanType)) {
        @SuppressWarnings("unchecked")
        Class<? extends IComponent> componentClass = (Class<? extends IComponent>) beanType;
        String simpleName = componentClass.getSimpleName();
        registry.put(simpleName, componentClass);
        log.debug("Registered component: {}", simpleName);
      }
    }

    log.info("ComponentRegistry initialized with {} components", registry.size());
  }

  /**
   * Retrieves the component class by its simple name.
   *
   * @param simpleName the simple class name of the component
   * @return the component class, or null if not found
   */
  public Class<? extends IComponent> getComponentClass(String simpleName) {
    return registry.get(simpleName);
  }

  /**
   * Returns all registered component names.
   *
   * @return collection of component simple names
   */
  public Collection<String> getComponentNames() {
    return registry.keySet();
  }

  /**
   * Returns the total number of registered components.
   *
   * @return number of registered components
   */
  public int size() {
    return registry.size();
  }
}
