package engine.services.world;

import engine.IService;
import io.micronaut.context.ApplicationContext;
import jakarta.inject.Singleton;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** ECS world service managing entities, components, and systems. */
@Singleton
public class WorldService implements IService {
  private final World world = new World();
  private final SystemManager systemManager = new SystemManager(world);

  private final ApplicationContext applicationContext;
  private final Map<Class<? extends ISystem>, ISystem> activeSystems = new ConcurrentHashMap<>();

  public WorldService(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }

  /** Execution order; lower values run earlier. */
  @Override
  public int executionOrder() {
    return 50;
  }

  /** Update all systems (no delta). */
  @Override
  public void update() {
    systemManager.update();
  }

  /** Update all systems with delta time. */
  @Override
  public void update(float dt) {
    systemManager.update(dt);
  }

  /** Create and return a new entity id. */
  public int createEntity() {
    return world.createEntity();
  }

  /** Destroy the given entity. */
  public void destroyEntity(int entityId) {
    world.destroyEntity(entityId);
  }

  /** Add a component to an entity. */
  public <T> void addComponent(int entityId, T component) {
    world.addComponent(entityId, component);
  }

  /** Get a component of the given type for an entity. */
  public <T> T getComponent(int entityId, Class<T> componentClass) {
    return world.getComponent(entityId, componentClass);
  }

  /** Remove a component type from an entity. */
  public void removeComponent(int entityId, Class<?> componentClass) {
    world.removeComponent(entityId, componentClass);
  }

  /** true if the entity has a component of the given type. */
  public boolean hasComponent(int entityId, Class<?> componentClass) {
    return world.hasComponent(entityId, componentClass);
  }

  /** Add a system instance. */
  public void addSystem(ISystem system) {
    if (system == null) return;
    Class<? extends ISystem> type = system.getClass();
    ISystem existing = activeSystems.put(type, system);
    if (existing != null && existing != system) {
      // Replace existing instance
      systemManager.removeSystem(existing);
    }
    systemManager.addSystem(system);
  }

  /** Remove a system instance. */
  public void removeSystem(ISystem system) {
    if (system == null) return;
    Class<? extends ISystem> type = system.getClass();
    ISystem existing = activeSystems.get(type);
    if (existing == system) {
      activeSystems.remove(type);
    }
    systemManager.removeSystem(system);
  }

  /** Enable a system by class (bean-backed). */
  public void enableSystem(Class<? extends ISystem> systemClass) {
    if (systemClass == null) return;
    activeSystems.computeIfAbsent(systemClass, cls -> {
      ISystem instance = getBean(cls);
      systemManager.addSystem(instance);
      return instance;
    });
  }

  /** Disable a system by class. */
  public void disableSystem(Class<? extends ISystem> systemClass) {
    if (systemClass == null) return;
    ISystem instance = activeSystems.remove(systemClass);
    if (instance != null) {
      systemManager.removeSystem(instance);
    }
  }

  /** Enable a list of systems. */
  @SafeVarargs
  public final void enableSystems(Class<? extends ISystem>... systemClasses) {
    if (systemClasses == null) return;
    for (Class<? extends ISystem> cls : systemClasses) enableSystem(cls);
  }

  /** Enable systems from an iterable. */
  public void enableSystems(Iterable<Class<? extends ISystem>> systemClasses) {
    if (systemClasses == null) return;
    for (Class<? extends ISystem> cls : systemClasses) enableSystem(cls);
  }

  /** Disable systems from an iterable. */
  public void disableSystems(Iterable<Class<? extends ISystem>> systemClasses) {
    if (systemClasses == null) return;
    for (Class<? extends ISystem> cls : systemClasses) disableSystem(cls);
  }

  /** Get an active system (enables it if missing). */
  public <T extends ISystem> T getSystem(Class<T> systemClass) {
    ISystem instance = activeSystems.get(systemClass);
    if (instance == null) {
      // Lazily enable if requested explicitly
      enableSystem(systemClass);
      instance = activeSystems.get(systemClass);
    }
    return systemClass.cast(instance);
  }

  private <T extends ISystem> T getBean(Class<T> systemClass) {
    // In Micronaut 4, ApplicationContext#getBean(Class) returns a new instance for @Prototype beans
    // and the existing instance for @Singleton beans.
    return applicationContext.getBean(systemClass);
  }

  /** Entities containing all of the given component types. */
  public Set<Integer> getEntitiesWith(Class<?>... componentClasses) {
    return world.getEntitiesWith(componentClasses);
  }

  /** Remove all systems. */
  public void clearSystems() {
    activeSystems.clear();
    systemManager.clear();
  }
}
