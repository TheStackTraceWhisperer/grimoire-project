package engine.services.world;

import java.util.HashSet;
import java.util.Set;

class EntityManager implements IEntityManager {
  private int nextEntityId = 0;
  private final Set<Integer> activeEntities = new HashSet<>();

  public int createEntity() {
    int id = nextEntityId++;
    activeEntities.add(id);
    return id;
  }

  public void destroyEntity(int entityId) {
    activeEntities.remove(entityId);
  }

  @Override
  public Set<Integer> getEntities() {
    return new HashSet<>(activeEntities);
  }

  public Set<Integer> getActiveEntities() {
    return getEntities();
  }
}
