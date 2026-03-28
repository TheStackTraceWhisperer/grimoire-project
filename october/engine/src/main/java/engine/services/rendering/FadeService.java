package engine.services.rendering;

import engine.IService;
import jakarta.inject.Singleton;

@Singleton
public class FadeService implements IService {
  private boolean fading = false;
  private float timer = 0f;
  private float duration = 0f;
  private String fadeType = ""; // e.g., IN/OUT

  @Override
  public int executionOrder() {
    return 32; // after UIRendererService
  }

  @Override
  public void update(float dt) {
    if (!fading) return;
    timer += dt;
    if (timer >= duration) {
      fading = false;
      timer = 0f;
      duration = 0f;
      fadeType = "";
    }
  }

  public void startFade(String type, float durationSeconds) {
    this.fadeType = type != null ? type : "";
    this.duration = Math.max(0f, durationSeconds);
    this.timer = 0f;
    this.fading = this.duration > 0f;
  }

  public boolean isFading() {
    return fading;
  }

  public String getFadeType() {
    return fadeType;
  }

  /**
   * Returns the normalized progress of the current fade (0..1). If no fade is active, returns 1.
   */
  public float getProgress() {
    if (!fading) return 1.0f;
    if (duration <= 0f) return 1.0f;
    float p = timer / duration;
    return p < 0f ? 0f : Math.min(1.0f, p);
  }
}
