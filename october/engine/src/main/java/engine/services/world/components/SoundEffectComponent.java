package engine.services.world.components;

import engine.services.world.IComponent;
import io.micronaut.core.annotation.Introspected;
import lombok.RequiredArgsConstructor;

@Introspected
@RequiredArgsConstructor
public class SoundEffectComponent implements IComponent {

  public interface SoundEffectType {
  }

  public final String soundBufferHandle;
  public final SoundEffectType soundType;

  public float volume = 1.0f;
  public float pitch = 1.0f;
  public boolean autoPlay = true;
  public boolean removeAfterPlay = true;

  public transient boolean hasBeenTriggered = false;
}
