package engine.services.world;

public interface ISystem {
  default void update(World world) { }
  default void update(World world, float deltaTime) { }
  default int priority() {
    return 0;
  }
}
