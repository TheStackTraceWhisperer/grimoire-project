package com.grimoire.domain.core.component;

/**
 * Bounding box component for collision detection.
 */
public class BoundingBox implements Component {

    /** The width of the bounding box in world units. */
    public double width;

    /** The height of the bounding box in world units. */
    public double height;

    /** No-arg constructor for array pre-allocation. */
    public BoundingBox() {
        // default values
    }

    /**
     * Creates a bounding box with the given dimensions.
     *
     * @param width
     *            the width
     * @param height
     *            the height
     */
    public BoundingBox(double width, double height) {
        this.width = width;
        this.height = height;
    }

    /**
     * Zero-allocation update.
     *
     * @param newWidth
     *            new width
     * @param newHeight
     *            new height
     */
    public void update(double newWidth, double newHeight) {
        this.width = newWidth;
        this.height = newHeight;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BoundingBox b)) {
            return false;
        }
        return Double.compare(b.width, width) == 0 && Double.compare(b.height, height) == 0;
    }

    @Override
    public int hashCode() {
        return Double.hashCode(width) * 31 + Double.hashCode(height);
    }

    @Override
    public String toString() {
        return "BoundingBox[width=" + width + ", height=" + height + "]";
    }
}
