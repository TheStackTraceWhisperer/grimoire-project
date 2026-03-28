package engine.services.world.components;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MeshComponentTest {

    @Test
    void constructor_shouldSetMeshHandle() {
        MeshComponent mesh = new MeshComponent("player_mesh");

        assertEquals("player_mesh", mesh.meshHandle());
    }

    @Test
    void constructor_shouldAcceptNullHandle() {
        MeshComponent mesh = new MeshComponent(null);

        assertNull(mesh.meshHandle());
    }

    @Test
    void equals_shouldReturnTrueForSameMeshHandle() {
        MeshComponent mesh1 = new MeshComponent("cube_mesh");
        MeshComponent mesh2 = new MeshComponent("cube_mesh");

        assertEquals(mesh1, mesh2);
    }

    @Test
    void equals_shouldReturnFalseForDifferentMeshHandle() {
        MeshComponent mesh1 = new MeshComponent("cube_mesh");
        MeshComponent mesh2 = new MeshComponent("sphere_mesh");

        assertNotEquals(mesh1, mesh2);
    }

    @Test
    void hashCode_shouldBeConsistentForEqualMeshes() {
        MeshComponent mesh1 = new MeshComponent("test_mesh");
        MeshComponent mesh2 = new MeshComponent("test_mesh");

        assertEquals(mesh1.hashCode(), mesh2.hashCode());
    }

    @Test
    void toString_shouldContainMeshHandle() {
        MeshComponent mesh = new MeshComponent("player_mesh");

        String toString = mesh.toString();
        assertTrue(toString.contains("player_mesh"));
    }
}
