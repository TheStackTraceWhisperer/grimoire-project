package engine.services.world.components;

import engine.services.world.IComponent;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ActiveSequenceComponentTest {

  @Test
  void testActiveSequenceComponentInitialization() {
    // Given a sequence ID
    String sequenceId = "test_sequence";

    // When we create an ActiveSequenceComponent
    ActiveSequenceComponent component = new ActiveSequenceComponent(sequenceId);

    // Then it should be initialized with correct default values
    assertThat(component.getSequenceId()).isEqualTo(sequenceId);
    assertThat(component.getCurrentIndex()).isEqualTo(0);
    assertThat(component.getWaitTimer()).isEqualTo(0.0f);
    assertThat(component.isBlocked()).isFalse();
  }

  @Test
  void testActiveSequenceComponentSetters() {
    // Given an ActiveSequenceComponent
    ActiveSequenceComponent component = new ActiveSequenceComponent("test");

    // When we update its properties
    component.setCurrentIndex(5);
    component.setWaitTimer(2.5f);
    component.setBlocked(true);

    // Then the values should be updated
    assertThat(component.getCurrentIndex()).isEqualTo(5);
    assertThat(component.getWaitTimer()).isEqualTo(2.5f);
    assertThat(component.isBlocked()).isTrue();
  }

  @Test
  void testActiveSequenceComponentImplementsIComponent() {
    // Given an ActiveSequenceComponent
    ActiveSequenceComponent component = new ActiveSequenceComponent("test");

    // Then it should implement IComponent
    assertThat(component).isInstanceOf(IComponent.class);
  }
}
