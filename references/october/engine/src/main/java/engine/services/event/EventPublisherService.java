package engine.services.event;

import engine.IService;
import io.micronaut.context.event.ApplicationEventPublisher;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;

@SuppressWarnings({"rawtypes", "unchecked"})
@Singleton
@RequiredArgsConstructor
public class EventPublisherService implements IService {
  private final ApplicationEventPublisher applicationEventPublisher;

  @Override
  public int executionOrder() {
    return 10;
  }

  public <T> void publish(T event) {
    applicationEventPublisher.publishEvent(event);
  }
}
