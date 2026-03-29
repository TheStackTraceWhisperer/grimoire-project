package application.ui;

import engine.services.rendering.UIRendererService;
import engine.services.window.WindowService;
import engine.services.world.ISystem;
import engine.services.world.World;
import engine.services.world.components.UITransformComponent;
import io.micronaut.context.annotation.Prototype;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;

import java.util.function.Supplier;

/** Render a simple progress bar overlay in a screen corner. */
@Prototype
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class TimerOverlaySystem implements ISystem {

  public enum AnchorCorner { TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT }

  private final WindowService windowService;
  private final UIRendererService uiRendererService;

  private Supplier<Float> progressSupplier = () -> 0f; // 0..1
  private AnchorCorner anchorCorner = AnchorCorner.BOTTOM_RIGHT;

  // Dimensions in pixels
  private float barWidth = 200f;
  private float barHeight = 10f;
  private float margin = 16f;
  private static final float BAR_PADDING = 2f; // inner padding for fill vs background

  // Colors RGBA
  private float bgR = 0f, bgG = 0f, bgB = 0f, bgA = 0.35f;
  private float fillR = 0.2f, fillG = 0.7f, fillB = 1f, fillA = 0.95f;
  private float borderR = 1f, borderG = 1f, borderB = 1f, borderA = 0.6f;
  private static final float BORDER_THICKNESS = 1f;

  public void setProgressSupplier(Supplier<Float> progressSupplier) {
    this.progressSupplier = progressSupplier != null ? progressSupplier : () -> 0f;
  }

  public void setAnchorCorner(AnchorCorner corner) {
    this.anchorCorner = corner != null ? corner : AnchorCorner.BOTTOM_RIGHT;
  }

  public void setFillColor(float r, float g, float b, float a) {
    this.fillR = r; this.fillG = g; this.fillB = b; this.fillA = a;
  }

  public void setBackgroundColor(float r, float g, float b, float a) {
    this.bgR = r; this.bgG = g; this.bgB = b; this.bgA = a;
  }

  public void setBorderColor(float r, float g, float b, float a) {
    this.borderR = r; this.borderG = g; this.borderB = b; this.borderA = a;
  }

  public void setDimensions(float width, float height) {
    this.barWidth = width; this.barHeight = height;
  }

  public void setMargin(float margin) { this.margin = margin; }

  // --- Minimal getters for tests ---
  public AnchorCorner getAnchorCorner() { return anchorCorner; }
  public float getBarWidth() { return barWidth; }
  public float getBarHeight() { return barHeight; }
  public float getMargin() { return margin; }

  @Override
  public int priority() {
    // Render above UI (10) and fade overlay (11)
    return 12;
  }

  @Override
  public void update(World world, float deltaTime) {
    float p;
    Supplier<Float> ps = this.progressSupplier;
    try {
      Float v = (ps != null) ? ps.get() : 0f;
      p = (v != null) ? v : 0f;
    } catch (Exception ignored) { p = 0f; }
    if (Float.isNaN(p) || Float.isInfinite(p)) p = 0f;
    if (p < 0f) p = 0f; if (p > 1f) p = 1f;

    int width = windowService.getWidth();
    int height = windowService.getHeight();

    // Compute outer rect position
    float outerW = barWidth;
    float outerH = barHeight;

    float minX, minY;
    switch (anchorCorner) {
      case TOP_LEFT -> { minX = margin; minY = height - margin - outerH; }
      case TOP_RIGHT -> { minX = width - margin - outerW; minY = height - margin - outerH; }
      case BOTTOM_LEFT -> { minX = margin; minY = margin; }
      case BOTTOM_RIGHT -> { minX = width - margin - outerW; minY = margin; }
      default -> { minX = width - margin - outerW; minY = margin; }
    }

    float maxX = minX + outerW;
    float maxY = minY + outerH;

    // Background rectangle
    UITransformComponent bg = new UITransformComponent();
    bg.relativeSize = false;
    bg.screenBounds[0] = minX;
    bg.screenBounds[1] = minY;
    bg.screenBounds[2] = maxX;
    bg.screenBounds[3] = maxY;

    // Border rectangles (top/bottom/left/right)
    UITransformComponent top = rect(minX, maxY - BORDER_THICKNESS, maxX, maxY);
    UITransformComponent bottom = rect(minX, minY, maxX, minY + BORDER_THICKNESS);
    UITransformComponent left = rect(minX, minY, minX + BORDER_THICKNESS, maxY);
    UITransformComponent right = rect(maxX - BORDER_THICKNESS, minY, maxX, maxY);

    // Fill rectangle (inside padding)
    float innerMinX = minX + BAR_PADDING;
    float innerMinY = minY + BAR_PADDING;
    float innerMaxX = maxX - BAR_PADDING;
    float innerMaxY = maxY - BAR_PADDING;

    float innerWidth = innerMaxX - innerMinX;
    float fillMaxX = innerMinX + innerWidth * p;

    UITransformComponent fill = rect(innerMinX, innerMinY, fillMaxX, innerMaxY);

    uiRendererService.begin();
    uiRendererService.submitColored(bg, "white", bgR, bgG, bgB, bgA);
    // Border
    uiRendererService.submitColored(top, "white", borderR, borderG, borderB, borderA);
    uiRendererService.submitColored(bottom, "white", borderR, borderG, borderB, borderA);
    uiRendererService.submitColored(left, "white", borderR, borderG, borderB, borderA);
    uiRendererService.submitColored(right, "white", borderR, borderG, borderB, borderA);
    // Fill
    if (p > 0f) {
      uiRendererService.submitColored(fill, "white", fillR, fillG, fillB, fillA);
    }
    uiRendererService.end();
  }

  private static UITransformComponent rect(float minX, float minY, float maxX, float maxY) {
    UITransformComponent t = new UITransformComponent();
    t.relativeSize = false;
    t.screenBounds[0] = minX;
    t.screenBounds[1] = minY;
    t.screenBounds[2] = maxX;
    t.screenBounds[3] = maxY;
    return t;
  }
}
