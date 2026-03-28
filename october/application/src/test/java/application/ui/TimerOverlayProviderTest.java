package application.ui;

import io.micronaut.context.BeanProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TimerOverlayProviderTest {

  private static class SimpleBeanProvider implements BeanProvider<TimerOverlaySystem> {
    @Override
    public TimerOverlaySystem get() {
      // We won't call update() in this test, so null deps are fine
      return new TimerOverlaySystem(null, null);
    }
  }

  @Test
  void mainMenuPreset_appliesExpectedDefaults() {
    TimerOverlayProvider provider = new TimerOverlayProvider(new SimpleBeanProvider());
    TimerOverlaySystem sys = provider.mainMenu(() -> 0.5f);

    assertEquals(TimerOverlaySystem.AnchorCorner.BOTTOM_RIGHT, sys.getAnchorCorner());
    assertEquals(220f, sys.getBarWidth());
    assertEquals(12f, sys.getBarHeight());
    assertEquals(20f, sys.getMargin());
  }

  @Test
  void introCutscenePreset_appliesExpectedDefaults() {
    TimerOverlayProvider provider = new TimerOverlayProvider(new SimpleBeanProvider());
    TimerOverlaySystem sys = provider.introCutscene(() -> 0.5f);

    assertEquals(TimerOverlaySystem.AnchorCorner.TOP_RIGHT, sys.getAnchorCorner());
    assertEquals(240f, sys.getBarWidth());
    assertEquals(12f, sys.getBarHeight());
    assertEquals(20f, sys.getMargin());
  }
}
