package engine.services.world.components;


import engine.services.world.IComponent;
import io.micronaut.core.annotation.Introspected;

/**
 * An ECS component that associates an entity with a renderable mesh.
 * <p>
 * This component is a simple data container that holds a string handle.
 * The RenderSystem uses this handle to look up the actual Mesh resource
 * from the ResourceManager.
 */
@Introspected
public record MeshComponent(String meshHandle) implements IComponent {

}
