package engine.services.world.systems;

import engine.services.event.EventPublisherService;
import engine.services.input.InputService;
import engine.services.rendering.UIRendererService;
import engine.services.window.WindowService;
import engine.services.world.World;
import engine.services.world.components.UIButtonComponent;
import engine.services.world.components.UIImageComponent;
import engine.services.world.components.UITransformComponent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UISystemTest {

    @Mock
    private WindowService window;
    @Mock
    private InputService inputService;
    @Mock
    private EventPublisherService eventPublisher;
    @Mock
    private UIRendererService uiRendererService;
    @Mock
    private World world;

    @InjectMocks
    private UISystem uiSystem;

    @Test
    void update_shouldCallBeginAndEndOnRenderer() {
        // Arrange
        when(world.getEntitiesWith(UITransformComponent.class)).thenReturn(Set.of());

        // Act
        uiSystem.update(world, 0.1f);

        // Assert
        verify(uiRendererService).begin();
        verify(uiRendererService).end();
    }

    @Test
    void update_shouldSubmitUIImageToRenderer() {
        // Arrange
        int entityId = 1;
        var transform = new UITransformComponent();
        var image = new UIImageComponent("test_image");

        when(world.getEntitiesWith(UITransformComponent.class)).thenReturn(Set.of(entityId));
        when(world.getComponent(entityId, UITransformComponent.class)).thenReturn(transform);

        // Specify behavior for each component check to test the correct path
        when(world.hasComponent(entityId, UIButtonComponent.class)).thenReturn(false);
        when(world.hasComponent(entityId, UIImageComponent.class)).thenReturn(true);

        when(world.getComponent(entityId, UIImageComponent.class)).thenReturn(image);

        // Act
        uiSystem.update(world, 0.1f);

        // Assert
        verify(uiRendererService).submit(transform, "test_image");
    }
}
