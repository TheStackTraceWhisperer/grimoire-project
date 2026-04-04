package com.grimoire.domain.core.component;

/**
 * Marker component indicating an entity is solid and blocks movement.
 *
 * <p>
 * Entities with this component cannot be passed through by other entities. Used
 * for walls, NPCs, and other obstacles.
 * </p>
 */
public class Solid implements Component {

    /** No-arg constructor. */
    public Solid() {
        // default values
    }

    @Override
    public boolean equals(Object o) {
        return this == o || o instanceof Solid;
    }

    @Override
    public int hashCode() {
        return 1;
    }

    @Override
    public String toString() {
        return "Solid[]";
    }
}
