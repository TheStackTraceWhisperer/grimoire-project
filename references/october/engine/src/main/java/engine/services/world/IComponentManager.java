package engine.services.world;

public interface IComponentManager {
  <T> void addComponent(int entityId, T component);
}
