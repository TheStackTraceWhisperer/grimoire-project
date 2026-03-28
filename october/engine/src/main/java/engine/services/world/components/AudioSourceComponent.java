package engine.services.world.components;

import engine.services.world.IComponent;
import io.micronaut.core.annotation.Introspected;
import lombok.RequiredArgsConstructor;

/**
 * A component that allows an entity to play audio through a positioned 3D audio source.
 */
@Introspected
@RequiredArgsConstructor
public class AudioSourceComponent implements IComponent {

    public final String audioBufferHandle;

    public float volume = 1.0f;
    public float pitch = 1.0f;
    public boolean looping = false;
    public boolean autoPlay = false;

    public transient boolean isPlaying = false;
}
