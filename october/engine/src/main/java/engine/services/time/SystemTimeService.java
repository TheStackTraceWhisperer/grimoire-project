package engine.services.time;


import engine.IService;
import jakarta.inject.Singleton;
import lombok.Getter;

@Singleton
public class SystemTimeService implements IService {

  private static final double NANOS_PER_SECOND = 1_000_000_000.0;

  private long startTimeNanos;
  private long lastFrameNanos;

  @Getter
  private float deltaTimeSeconds;

  @Getter
  private double totalTimeSeconds;

  @Override
  public int executionOrder() {
    return 10;
  }

  @Override
  public void update() {
    long currentFrameNanos = System.nanoTime();

    // Calculate delta time
    deltaTimeSeconds = (float) ((currentFrameNanos - lastFrameNanos) / NANOS_PER_SECOND);

    // Calculate total elapsed time
    totalTimeSeconds = (currentFrameNanos - startTimeNanos) / NANOS_PER_SECOND;

    // Update last frame time for the next iteration
    lastFrameNanos = currentFrameNanos;
  }

  @Override
  public void start() {
    startTimeNanos = System.nanoTime();
    lastFrameNanos = startTimeNanos;
  }
}
