package com.grimoire.domain.core.component;

/**
 * Renderable component for visual representation of an entity.
 */
public class Renderable implements Component {

    /**
     * The display name.
     */
    public String name;

    /**
     * The identifier for the visual asset (sprite, model, etc.).
     */
    public String visualId;

    /**
     * No-arg constructor for array pre-allocation.
     */
    public Renderable() {
        // default values
    }

    /**
     * Creates a renderable.
     *
     * @param name
     *            the display name
     * @param visualId
     *            the visual asset ID
     */
    public Renderable(String name, String visualId) {
        this.name = name;
        this.visualId = visualId;
    }

    /**
     * Zero-allocation update.
     *
     * @param newName
     *            new display name
     * @param newVisualId
     *            new visual asset ID
     */
    public void update(String newName, String newVisualId) {
        this.name = newName;
        this.visualId = newVisualId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Renderable r)) {
            return false;
        }
        return java.util.Objects.equals(name, r.name)
                && java.util.Objects.equals(visualId, r.visualId);
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        return 31 * result + (visualId != null ? visualId.hashCode() : 0);
    }

    @Override
    public String toString() {
        return "Renderable[name=" + name + ", visualId=" + visualId + "]";
    }
}
