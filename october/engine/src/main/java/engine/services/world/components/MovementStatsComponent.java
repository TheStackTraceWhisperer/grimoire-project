package engine.services.world.components;


import engine.services.world.IComponent;
import io.micronaut.core.annotation.Introspected;

/**
 * A component that stores movement-related stats for an entity.
 */
@Introspected
public record MovementStatsComponent(float speed) implements IComponent {
}
