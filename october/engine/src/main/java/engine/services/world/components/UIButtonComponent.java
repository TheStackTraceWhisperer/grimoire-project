package engine.services.world.components;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import engine.services.world.IComponent;
import io.micronaut.core.annotation.Introspected;
import org.joml.Vector4f;

@Introspected
public final class UIButtonComponent implements IComponent {

  public final String actionEvent;
  public final String normalTexture;
  public final String hoveredTexture;
  public final String pressedTexture;
  public final Vector4f normalColor;
  public final Vector4f hoveredColor;
  public final Vector4f pressedColor;

  public enum ButtonState {
    NORMAL,
    HOVERED,
    PRESSED
  }

  public transient ButtonState currentState = ButtonState.NORMAL;

  @JsonCreator
  public UIButtonComponent(
    @JsonProperty("actionEvent") String actionEvent,
    @JsonProperty("normalTexture") String normalTexture,
    @JsonProperty("hoveredTexture") String hoveredTexture,
    @JsonProperty("pressedTexture") String pressedTexture,
    @JsonProperty("normalColor") Vector4f normalColor,
    @JsonProperty("hoveredColor") Vector4f hoveredColor,
    @JsonProperty("pressedColor") Vector4f pressedColor) {
    this.actionEvent = actionEvent;
    this.normalTexture = normalTexture;
    this.hoveredTexture = hoveredTexture;
    this.pressedTexture = pressedTexture;
    this.normalColor = normalColor != null ? normalColor : new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
    this.hoveredColor = hoveredColor != null ? hoveredColor : new Vector4f(0.9f, 0.9f, 0.9f, 1.0f);
    this.pressedColor = pressedColor != null ? pressedColor : new Vector4f(0.7f, 0.7f, 0.7f, 1.0f);
  }
}
