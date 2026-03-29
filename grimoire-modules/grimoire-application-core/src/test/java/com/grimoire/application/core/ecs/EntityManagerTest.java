package com.grimoire.application.core.ecs;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EntityManagerTest {

    private EntityManager manager;

    @BeforeEach
    void setUp() {
        manager = new EntityManager();
    }

    @Test
    void createEntityReturnsNonNullId() {
        String id = manager.createEntity();

        assertThat(id).isNotNull().isNotBlank();
    }

    @Test
    void createdEntityExists() {
        String id = manager.createEntity();

        assertThat(manager.exists(id)).isTrue();
    }

    @Test
    void multipleEntitiesHaveUniqueIds() {
        String id1 = manager.createEntity();
        String id2 = manager.createEntity();

        assertThat(id1).isNotEqualTo(id2);
    }

    @Test
    void destroyEntityRemovesIt() {
        String id = manager.createEntity();
        manager.destroyEntity(id);

        assertThat(manager.exists(id)).isFalse();
    }

    @Test
    void destroyNonExistentEntityDoesNotThrow() {
        manager.destroyEntity("does-not-exist");
    }

    @Test
    void existsReturnsFalseForUnknownId() {
        assertThat(manager.exists("unknown")).isFalse();
    }

    @Test
    void getAllEntityIdsReflectsLiveEntities() {
        String id1 = manager.createEntity();
        String id2 = manager.createEntity();

        assertThat(manager.getAllEntityIds()).containsExactlyInAnyOrder(id1, id2);
    }

    @Test
    void getAllEntityIdsEmptyInitially() {
        assertThat(manager.getAllEntityIds()).isEmpty();
    }

    @Test
    void getAllEntityIdsExcludesDestroyed() {
        String id1 = manager.createEntity();
        String id2 = manager.createEntity();
        manager.destroyEntity(id1);

        assertThat(manager.getAllEntityIds()).containsExactly(id2);
    }
}
