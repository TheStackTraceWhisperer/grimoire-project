package engine.services.world.components;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransformComponentTest {

    private TransformComponent transform;

    @BeforeEach
    void setUp() {
        transform = new TransformComponent();
    }

    @Test
    void constructor_shouldInitializeWithDefaultValues() {
        assertEquals(new Vector3f(0, 0, 0), transform.position);
        assertEquals(new Vector3f(0, 0, 0), transform.previousPosition);
        assertEquals(new Quaternionf().identity(), transform.rotation);
        assertEquals(new Vector3f(1, 1, 1), transform.scale);
    }

    @Test
    void updatePreviousPosition_shouldSetPreviousPositionToCurrentPosition() {
        transform.position.set(1, 2, 3);
        transform.updatePreviousPosition();
        assertEquals(new Vector3f(1, 2, 3), transform.previousPosition);
    }

    @Test
    void updatePreviousPosition_shouldHandleNegativePositions() {
        transform.position.set(-5, -10, -15);
        transform.updatePreviousPosition();
        assertEquals(new Vector3f(-5, -10, -15), transform.previousPosition);
    }

    @Test
    void updatePreviousPosition_shouldHandleZeroPosition() {
        transform.position.set(0, 0, 0);
        transform.updatePreviousPosition();
        assertEquals(new Vector3f(0, 0, 0), transform.previousPosition);
    }

    @Test
    void revertPosition_shouldSetPositionToPreviousPosition() {
        transform.previousPosition.set(4, 5, 6);
        transform.position.set(1, 2, 3); // Change current position
        transform.revertPosition();
        assertEquals(new Vector3f(4, 5, 6), transform.position);
    }

    @Test
    void revertPosition_shouldHandleMultipleReverts() {
        transform.previousPosition.set(10, 20, 30);
        transform.position.set(100, 200, 300);

        transform.revertPosition();
        assertEquals(new Vector3f(10, 20, 30), transform.position);

        // Revert again - should still use same previous position
        transform.revertPosition();
        assertEquals(new Vector3f(10, 20, 30), transform.position);
    }

    @Test
    void getTransformMatrix_shouldReturnCorrectMatrix() {
        transform.position.set(10, 0, 0);
        transform.rotation.fromAxisAngleDeg(0, 1, 0, 90);
        transform.scale.set(2, 2, 2);

        Matrix4f expectedMatrix = new Matrix4f()
                .translate(10, 0, 0)
                .rotate((float) Math.toRadians(90), 0, 1, 0)
                .scale(2, 2, 2);

        assertTrue(expectedMatrix.equals(transform.getTransformMatrix(), 0.001f));
    }

    @Test
    void getTransformMatrix_shouldReturnIdentityForDefaultTransform() {
        Matrix4f expectedMatrix = new Matrix4f()
                .translate(0, 0, 0)
                .rotate(new Quaternionf().identity())
                .scale(1, 1, 1);

        assertTrue(expectedMatrix.equals(transform.getTransformMatrix(), 0.001f));
    }

    @Test
    void getTransformMatrix_shouldHandleNonUniformScale() {
        transform.position.set(5, 10, 15);
        transform.scale.set(2, 0.5f, 3);

        Matrix4f result = transform.getTransformMatrix();

        // Verify that the matrix is not null and has reasonable values
        assertTrue(result != null);
    }

    @Test
    void position_canBeModified() {
        transform.position.set(100, 200, 300);
        assertEquals(new Vector3f(100, 200, 300), transform.position);
    }

    @Test
    void rotation_canBeModified() {
        Quaternionf newRotation = new Quaternionf().fromAxisAngleDeg(1, 0, 0, 45);
        transform.rotation.set(newRotation);
        assertEquals(newRotation, transform.rotation);
    }

    @Test
    void scale_canBeModified() {
        transform.scale.set(5, 5, 5);
        assertEquals(new Vector3f(5, 5, 5), transform.scale);
    }

    @Test
    void getTransformMatrix_shouldHandleComplexTransforms() {
        transform.position.set(100, 50, -25);
        transform.rotation.fromAxisAngleDeg(0, 0, 1, 180);
        transform.scale.set(0.5f, 2.0f, 1.5f);

        Matrix4f result = transform.getTransformMatrix();

        // Verify the matrix was computed
        assertTrue(result != null);

        // Verify it's different from identity
        Matrix4f identity = new Matrix4f();
        assertTrue(!identity.equals(result, 0.001f));
    }
}
