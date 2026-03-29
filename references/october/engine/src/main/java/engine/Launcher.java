package engine;

import io.micronaut.context.ApplicationContext;
import io.micronaut.runtime.Micronaut;

public class Launcher {
  public static void run(Class<?> primarySource, String[] args) {
    try (
      ApplicationContext context = Micronaut
        .build(args)
        .mainClass(primarySource)
        .packages("engine", "application") // Scan both engine and application packages
        .banner(false)
        .start()
    ) {
      Engine engine = context.getBean(Engine.class);
      engine.run();
    }
  }
}
