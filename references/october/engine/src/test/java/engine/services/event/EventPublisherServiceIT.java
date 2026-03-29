package engine.services.event;

import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest
public class EventPublisherServiceIT {

    @Inject
    private EventPublisherService eventPublisherService;

    @Inject
    private TestEventListener testEventListener;

    // 1. Define a simple event class for testing
    private record MyTestEvent(String message) {}

    // 2. Define a listener bean that Micronaut will discover
    @Singleton
    static class TestEventListener implements ApplicationEventListener<MyTestEvent> {
        private boolean wasCalled = false;
        private String receivedMessage = "";

        @Override
        public void onApplicationEvent(MyTestEvent event) {
            this.wasCalled = true;
            this.receivedMessage = event.message();
        }

        public boolean wasCalled() {
            return wasCalled;
        }

        public String getReceivedMessage() {
            return receivedMessage;
        }
    }

    @Test
    void testPublishAndSubscribe() {
        // Given: A listener is subscribed to MyTestEvent via the Micronaut context
        String testMessage = "Hello, World!";
        MyTestEvent testEvent = new MyTestEvent(testMessage);

        // When: We publish the event through our service
        eventPublisherService.publish(testEvent);

        // Then: The listener bean should have been invoked and received the correct message
        assertTrue(testEventListener.wasCalled(), "The event listener should have been called.");
        assertEquals(testMessage, testEventListener.getReceivedMessage(), "The listener should have received the correct event message.");
    }
}
