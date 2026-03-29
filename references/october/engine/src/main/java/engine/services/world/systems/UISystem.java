package engine.services.world.systems;

import engine.services.event.EventPublisherService;
import engine.services.input.InputService;
import engine.services.rendering.UIRendererService;
import engine.services.window.WindowService;
import engine.services.world.ISystem;
import engine.services.world.World;
import engine.services.world.components.UIButtonComponent;
import engine.services.world.components.UIImageComponent;
import engine.services.world.components.UITransformComponent;
import io.micronaut.context.annotation.Prototype;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;

@Prototype
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class UISystem implements ISystem {

  private final WindowService window;
  private final InputService inputService;
  private final EventPublisherService eventPublisher;
  private final UIRendererService uiRendererService;

  @Override
  public int priority() {
    return 10; // Render UI on top of the game world
  }

  @Override
  public void update(World world, float deltaTime) {
    var uiEntities = world.getEntitiesWith(UITransformComponent.class);

    int width = window.getWidth();
    int height = window.getHeight();

    calculateLayout(world, uiEntities, width, height);
    handleButtonInteractions(world, uiEntities, height);
    renderUI(world, uiEntities);
  }

  private void calculateLayout(World world, Iterable<Integer> entities, int windowWidth, int windowHeight) {
    Vector2f parentSize = new Vector2f(windowWidth, windowHeight);

    for (int entityId : entities) {
      var transform = world.getComponent(entityId, UITransformComponent.class);

      float actualWidth, actualHeight;
      if (transform.relativeSize) {
        actualWidth = parentSize.x * transform.size.x;
        actualHeight = parentSize.y * transform.size.y;
      } else {
        actualWidth = transform.size.x;
        actualHeight = transform.size.y;
      }

      float anchorPosX = parentSize.x * transform.anchor.x;
      float anchorPosY = parentSize.y * transform.anchor.y;
      float pivotPosX = actualWidth * transform.pivot.x;
      float pivotPosY = actualHeight * transform.pivot.y;

      float minX = anchorPosX - pivotPosX + transform.offset.x;
      float minY = anchorPosY - pivotPosY + transform.offset.y;
      float maxX = minX + actualWidth;
      float maxY = minY + actualHeight;

      transform.screenBounds[0] = minX;
      transform.screenBounds[1] = minY;
      transform.screenBounds[2] = maxX;
      transform.screenBounds[3] = maxY;
    }
  }

  private void handleButtonInteractions(World world, Iterable<Integer> entities, int windowHeight) {
    double mouseX = inputService.getMouseX();
    double mouseY = windowHeight - inputService.getMouseY(); // Flip Y for OpenGL coords
    boolean isMouseDown = inputService.isMouseButtonPressed(GLFW.GLFW_MOUSE_BUTTON_LEFT);

    for (int entityId : entities) {
      if (!world.hasComponent(entityId, UIButtonComponent.class)) {
        continue;
      }

      var transform = world.getComponent(entityId, UITransformComponent.class);
      var button = world.getComponent(entityId, UIButtonComponent.class);

      float[] bounds = transform.screenBounds;
      boolean isHovered = mouseX >= bounds[0] && mouseX <= bounds[2] && mouseY >= bounds[1] && mouseY <= bounds[3];

      UIButtonComponent.ButtonState previousState = button.currentState;
      if (isHovered) {
        if (isMouseDown) {
          button.currentState = UIButtonComponent.ButtonState.PRESSED;
        } else {
          if (previousState == UIButtonComponent.ButtonState.PRESSED) {
            eventPublisher.publish(button.actionEvent);
          }
          button.currentState = UIButtonComponent.ButtonState.HOVERED;
        }
      } else {
        button.currentState = UIButtonComponent.ButtonState.NORMAL;
      }
    }
  }

  private void renderUI(World world, Iterable<Integer> entities) {
    uiRendererService.begin();
    for (int entityId : entities) {
      var transform = world.getComponent(entityId, UITransformComponent.class);
      if (world.hasComponent(entityId, UIButtonComponent.class)) {
        var button = world.getComponent(entityId, UIButtonComponent.class);
        String textureHandle = switch (button.currentState) {
          case HOVERED -> button.hoveredTexture;
          case PRESSED -> button.pressedTexture;
          default -> button.normalTexture;
        };
        uiRendererService.submit(transform, textureHandle);
      } else if (world.hasComponent(entityId, UIImageComponent.class)) {
        var image = world.getComponent(entityId, UIImageComponent.class);
        uiRendererService.submit(transform, image.textureHandle);
      }
    }
    uiRendererService.end();
  }
}
