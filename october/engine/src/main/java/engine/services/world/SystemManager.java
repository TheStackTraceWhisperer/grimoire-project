package engine.services.world;

import lombok.RequiredArgsConstructor;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@RequiredArgsConstructor
class SystemManager {
  private final World world;

  private final List<ISystem> systems = new CopyOnWriteArrayList<>();
  private boolean sorted = true;

  public void addSystem(ISystem system) {
    systems.add(system);
    sorted = false;
  }

  public void removeSystem(ISystem system) {
    systems.remove(system);
  }

  public void update() {
    if(!sorted) {
      systems.sort(Comparator.comparingInt(ISystem::priority));
      sorted = true;
    }

    for (ISystem system : systems) {
      system.update(world);
    }
  }

  public void update(float dt) {
    if(!sorted) {
      systems.sort(Comparator.comparingInt(ISystem::priority));
      sorted = true;
    }

    for (ISystem system : systems) {
      system.update(world, dt);
    }
  }

  public void clear() {
    systems.clear();
  }
}
