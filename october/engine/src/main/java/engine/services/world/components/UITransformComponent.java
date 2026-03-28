package engine.services.world.components;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import engine.services.world.IComponent;
import io.micronaut.core.annotation.Introspected;
import org.joml.Vector2f;
import org.joml.Vector3f;

@Introspected
public class UITransformComponent implements IComponent {

  public final Vector2f anchor;
  public final Vector2f pivot;
  public final Vector2f size;
  public final Vector3f offset;
  public boolean relativeSize = false;

  @JsonIgnore
  public final float[] screenBounds = new float[4];

  public UITransformComponent() {
    this.anchor = new Vector2f(0.5f, 0.5f);
    this.pivot = new Vector2f(0.5f, 0.5f);
    this.size = new Vector2f(100.0f, 100.0f);
    this.offset = new Vector3f(0.0f, 0.0f, 0.0f);
  }

  @JsonCreator
  public UITransformComponent(
    @JsonProperty("anchor") Vector2f anchor,
    @JsonProperty("pivot") Vector2f pivot,
    @JsonProperty("size") Vector2f size,
    @JsonProperty("offset") Vector3f offset) {
    this.anchor = anchor != null ? anchor : new Vector2f(0.5f, 0.5f);
    this.pivot = pivot != null ? pivot : new Vector2f(0.5f, 0.5f);
    this.size = size != null ? size : new Vector2f(100, 30);
    this.offset = offset != null ? offset : new Vector3f(0, 0, 0);
  }
}
