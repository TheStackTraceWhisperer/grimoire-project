package engine.services.world.components;

import engine.services.world.IComponent;
import io.micronaut.core.annotation.Introspected;
import lombok.RequiredArgsConstructor;

/**
 * A component for background music with advanced playback control.
 */
@Introspected
@RequiredArgsConstructor
public class MusicComponent implements IComponent {

    public final String musicBufferHandle;
    public float baseVolume = 1.0f;
    public boolean looping = true;
    public boolean autoPlay = true;
    public float fadeDuration = 2.0f;

    public transient float currentVolume = 1.0f;
    public transient boolean fadingIn = false;
    public transient boolean fadingOut = false;
    public transient float fadeTimer = 0.0f;
    public transient boolean isPlaying = false;
    public transient boolean isPaused = false;

    public void startFadeIn() {
        this.fadingIn = true;
        this.fadingOut = false;
        this.fadeTimer = 0.0f;
        this.currentVolume = 0.0f;
    }

    public void startFadeOut() {
        this.fadingOut = true;
        this.fadingIn = false;
        this.fadeTimer = 0.0f;
    }

    public void stopFade() {
        this.fadingIn = false;
        this.fadingOut = false;
        this.fadeTimer = 0.0f;
        this.currentVolume = this.baseVolume;
    }
}
