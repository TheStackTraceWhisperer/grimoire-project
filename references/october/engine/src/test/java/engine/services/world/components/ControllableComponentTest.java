package engine.services.world.components;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ControllableComponentTest {

    @Test
    void constructor_shouldInitializeWithDefaultValues() {
        ControllableComponent controllable = new ControllableComponent();

        assertEquals(0, controllable.playerId);
        assertFalse(controllable.wantsToMoveUp);
        assertFalse(controllable.wantsToMoveDown);
        assertFalse(controllable.wantsToMoveLeft);
        assertFalse(controllable.wantsToMoveRight);
        assertFalse(controllable.wantsToAttack);
    }

    @Test
    void playerId_canBeModified() {
        ControllableComponent controllable = new ControllableComponent();

        controllable.playerId = 1;
        assertEquals(1, controllable.playerId);

        controllable.playerId = 5;
        assertEquals(5, controllable.playerId);
    }

    @Test
    void movementFlags_canBeToggled() {
        ControllableComponent controllable = new ControllableComponent();

        controllable.wantsToMoveUp = true;
        assertTrue(controllable.wantsToMoveUp);

        controllable.wantsToMoveDown = true;
        assertTrue(controllable.wantsToMoveDown);

        controllable.wantsToMoveLeft = true;
        assertTrue(controllable.wantsToMoveLeft);

        controllable.wantsToMoveRight = true;
        assertTrue(controllable.wantsToMoveRight);
    }

    @Test
    void attackFlag_canBeToggled() {
        ControllableComponent controllable = new ControllableComponent();

        controllable.wantsToAttack = true;
        assertTrue(controllable.wantsToAttack);

        controllable.wantsToAttack = false;
        assertFalse(controllable.wantsToAttack);
    }

    @Test
    void allFlags_canBeSetSimultaneously() {
        ControllableComponent controllable = new ControllableComponent();

        controllable.wantsToMoveUp = true;
        controllable.wantsToMoveRight = true;
        controllable.wantsToAttack = true;

        assertTrue(controllable.wantsToMoveUp);
        assertTrue(controllable.wantsToMoveRight);
        assertTrue(controllable.wantsToAttack);
        assertFalse(controllable.wantsToMoveDown);
        assertFalse(controllable.wantsToMoveLeft);
    }

    @Test
    void flags_canBeResetToDefault() {
        ControllableComponent controllable = new ControllableComponent();

        // Set all flags
        controllable.wantsToMoveUp = true;
        controllable.wantsToMoveDown = true;
        controllable.wantsToMoveLeft = true;
        controllable.wantsToMoveRight = true;
        controllable.wantsToAttack = true;

        // Reset all flags
        controllable.wantsToMoveUp = false;
        controllable.wantsToMoveDown = false;
        controllable.wantsToMoveLeft = false;
        controllable.wantsToMoveRight = false;
        controllable.wantsToAttack = false;

        assertFalse(controllable.wantsToMoveUp);
        assertFalse(controllable.wantsToMoveDown);
        assertFalse(controllable.wantsToMoveLeft);
        assertFalse(controllable.wantsToMoveRight);
        assertFalse(controllable.wantsToAttack);
    }

    @Test
    void playerId_canBeNegative() {
        // While not typical, test that there's no restriction
        ControllableComponent controllable = new ControllableComponent();

        controllable.playerId = -1;
        assertEquals(-1, controllable.playerId);
    }
}
