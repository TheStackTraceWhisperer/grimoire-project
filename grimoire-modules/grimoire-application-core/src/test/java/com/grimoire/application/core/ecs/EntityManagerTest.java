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
    void createEntityReturnsNonNegativeId() {
        int id = manager.createEntity();

        assertThat(id).isGreaterThanOrEqualTo(0);
    }

    @Test
    void createdEntityExists() {
        int id = manager.createEntity();

        assertThat(manager.exists(id)).isTrue();
    }

    @Test
    void multipleEntitiesHaveSequentialIds() {
        int id1 = manager.createEntity();
        int id2 = manager.createEntity();

        assertThat(id2).isEqualTo(id1 + 1);
    }

    @Test
    void destroyEntityRemovesIt() {
        int id = manager.createEntity();
        manager.destroyEntity(id);

        assertThat(manager.exists(id)).isFalse();
    }

    @Test
    void destroyNonExistentEntityDoesNotThrow() {
        manager.destroyEntity(9999);
    }

    @Test
    void existsReturnsFalseForUnknownId() {
        assertThat(manager.exists(9999)).isFalse();
    }

    @Test
    void maxEntityIdTracksHighWaterMark() {
        manager.createEntity();
        manager.createEntity();
        manager.createEntity();

        assertThat(manager.getMaxEntityId()).isEqualTo(3);
    }

    @Test
    void aliveArrayReflectsState() {
        int id = manager.createEntity();

        assertThat(manager.getAlive()[id]).isTrue();

        manager.destroyEntity(id);

        assertThat(manager.getAlive()[id]).isFalse();
    }
}
