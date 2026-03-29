package engine.services.glfw;

import engine.EngineTestHarness;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class GlfwContextServiceIT extends EngineTestHarness {

    @Inject
    private GlfwContextService glfwContextService;

    @Test
    void testServiceIsInitializedByEngine() {
        // Given: The EngineTestHarness has started the engine

        // Then: The GlfwContextService, having the earliest execution order, should be initialized.
        assertTrue(glfwContextService.isInitialized(),
                "GlfwContextService should be initialized as part of the engine startup sequence.");
    }
}
