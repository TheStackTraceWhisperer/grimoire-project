package engine.game;

import engine.services.world.IComponent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EnemyComponentTest {

    @Test
    void constructor_shouldCreateEnemyComponent() {
        EnemyComponent enemy = new EnemyComponent();

        assertNotNull(enemy);
    }

    @Test
    void isInstanceOfIComponent() {
        EnemyComponent enemy = new EnemyComponent();

        assertTrue(enemy instanceof IComponent);
    }

    @Test
    void equals_shouldReturnTrueForAllInstances() {
        EnemyComponent enemy1 = new EnemyComponent();
        EnemyComponent enemy2 = new EnemyComponent();

        // Records with no fields are equal to all instances of the same type
        assertEquals(enemy1, enemy2);
    }

    @Test
    void hashCode_shouldBeConsistent() {
        EnemyComponent enemy1 = new EnemyComponent();
        EnemyComponent enemy2 = new EnemyComponent();

        assertEquals(enemy1.hashCode(), enemy2.hashCode());
    }

    @Test
    void toString_shouldNotBeNull() {
        EnemyComponent enemy = new EnemyComponent();

        assertNotNull(enemy.toString());
    }
}
