package com.grimoire.server.component;
import com.grimoire.ecs.Component;

/**
 * Renderable component for visual representation.
 */
public record Renderable(String name, String visualId) implements Component {
}
