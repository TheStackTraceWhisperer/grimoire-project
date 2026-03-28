package engine.services.window;

import jakarta.inject.Singleton;

@Singleton
public class WindowDefaults {

  public int width() {
    return 1024;
  }

  public int height() {
    return 768;
  }

  public String title() {
    return "October";
  }

}
