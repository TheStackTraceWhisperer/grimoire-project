package engine.services.world.components;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerComponentTest {

    @Test
    void constructor_shouldCreatePlayerComponent() {
        PlayerComponent player = new PlayerComponent();

        assertNotNull(player);
    }

    @Test
    void isInstanceOfIComponent() {
        PlayerComponent player = new PlayerComponent();

        assertTrue(player instanceof engine.services.world.IComponent);
    }

    @Test
    void multipleInstances_shouldBeDistinct() {
        PlayerComponent player1 = new PlayerComponent();
        PlayerComponent player2 = new PlayerComponent();

        assertNotSame(player1, player2);
    }
}
