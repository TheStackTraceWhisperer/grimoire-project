package engine.services.rendering;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CameraServiceTest {

    private CameraService cameraService;

    @BeforeEach
    void setUp() {
        cameraService = new CameraService();
    }

    @Test
    void testDefaultConstructorInitializesOrthographicProjection() {
        // Given a camera created with the default constructor
        // When we get its projection matrix
        Matrix4f projection = cameraService.getProjectionMatrix();

        // Then it should be an orthographic projection with a 16:9 aspect ratio
        Matrix4f expected = new Matrix4f().ortho(-8.0f, 8.0f, -4.5f, 4.5f, -1.0f, 100.0f);
        assertEquals(expected, projection);
    }

    @Test
    void testSetPositionUpdatesViewMatrix() {
        // Given a new position
        Vector3f newPosition = new Vector3f(10.0f, 5.0f, 20.0f);

        // When we set the camera's position
        cameraService.setPosition(newPosition);

        // Then the view matrix should be updated to reflect this new position
        Matrix4f viewMatrix = cameraService.getViewMatrix();
        Matrix4f expected = new Matrix4f().lookAt(newPosition, new Vector3f(10.0f, 5.0f, 19.0f), new Vector3f(0.0f, 1.0f, 0.0f));
        assertEquals(expected, viewMatrix);
    }

    @Test
    void testSetPerspectiveUpdatesProjectionMatrix() {
        // Given perspective parameters
        float fov = 60.0f;
        float aspectRatio = 16.0f / 9.0f;
        float near = 0.1f;
        float far = 1000.0f;

        // When we set the camera to a perspective projection
        cameraService.setPerspective(fov, aspectRatio, near, far);

        // Then the projection matrix should be updated accordingly
        Matrix4f projection = cameraService.getProjectionMatrix();
        Matrix4f expected = new Matrix4f().perspective((float) Math.toRadians(fov), aspectRatio, near, far);
        assertEquals(expected, projection);
    }

    @Test
    void testResizeUpdatesOrthographicProjection() {
        // Given a camera with a 16:9 ortho projection
        cameraService.setOrthographic(16, 9);

        // When we resize the screen to a wider aspect ratio (e.g., 1920x1080)
        cameraService.resize(1920, 1080);

        // Then the projection should be letterboxed correctly to maintain the 16:9 ratio
        Matrix4f projection = cameraService.getProjectionMatrix();
        // The height remains -4.5 to 4.5, width expands to match aspect ratio
        float expectedHalfWidth = 4.5f * (1920.0f / 1080.0f);
        Matrix4f expected = new Matrix4f().ortho(-expectedHalfWidth, expectedHalfWidth, -4.5f, 4.5f, -1.0f, 100.0f);
        assertEquals(expected, projection);
    }
}
