package engine.services.world.components;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import engine.services.world.IComponent;
import io.micronaut.core.annotation.Introspected;
import org.joml.Vector4f;

@Introspected
public class UIImageComponent implements IComponent {
  public String textureHandle;
  public final Vector4f color;

  @JsonCreator
  public UIImageComponent(
    @JsonProperty("textureHandle") String textureHandle,
    @JsonProperty("color") Vector4f color) {
    this.textureHandle = textureHandle;
    this.color = color != null ? color : new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
  }

  public UIImageComponent(String textureHandle) {
    this(textureHandle, null);
  }
}
