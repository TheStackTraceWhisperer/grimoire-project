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

    // ── ID recycling tests ──

    @Test
    void destroyedIdIsRecycled() {
        int id1 = manager.createEntity();
        manager.destroyEntity(id1);

        int id2 = manager.createEntity();
        assertThat(id2).isEqualTo(id1);
        assertThat(manager.exists(id2)).isTrue();
    }

    @Test
    void recycledIdsAreUsedBeforeFreshIds() {
        int a = manager.createEntity();
        int b = manager.createEntity();
        int c = manager.createEntity(); // fresh IDs: 0, 1, 2

        manager.destroyEntity(b); // free-list: [1]
        manager.destroyEntity(a); // free-list: [1, 0]

        // Next creates should recycle from the free-list (LIFO)
        int d = manager.createEntity();
        assertThat(d).isEqualTo(a); // popped 0

        int e = manager.createEntity();
        assertThat(e).isEqualTo(b); // popped 1

        // Free-list exhausted — next should be a fresh ID
        int f = manager.createEntity();
        assertThat(f).isEqualTo(c + 1); // fresh: 3

        assertThat(manager.exists(a)).isTrue();
        assertThat(manager.exists(b)).isTrue();
        assertThat(manager.exists(c)).isTrue();
        assertThat(manager.exists(f)).isTrue();
    }

    @Test
    void freeCountTracksRecyclableIds() {
        assertThat(manager.getFreeCount()).isZero();

        int a = manager.createEntity();
        int b = manager.createEntity();
        manager.destroyEntity(a);
        assertThat(manager.getFreeCount()).isEqualTo(1);

        manager.destroyEntity(b);
        assertThat(manager.getFreeCount()).isEqualTo(2);

        manager.createEntity(); // recycle one
        assertThat(manager.getFreeCount()).isEqualTo(1);
    }

    // ── High-water mark shrinking tests ──

    @Test
    void maxEntityIdShrinksWhenTrailingEntityDestroyed() {
        manager.createEntity(); // 0
        manager.createEntity(); // 1
        int last = manager.createEntity(); // 2
        assertThat(manager.getMaxEntityId()).isEqualTo(3);

        manager.destroyEntity(last);
        assertThat(manager.getMaxEntityId()).isEqualTo(2);
    }

    @Test
    void maxEntityIdShrinksOverConsecutiveDeadTrail() {
        int a = manager.createEntity(); // 0
        manager.createEntity(); // 1
        manager.createEntity(); // 2
        assertThat(manager.getMaxEntityId()).isEqualTo(3);

        // Destroy middle and last — leave only entity 0
        manager.destroyEntity(1);
        assertThat(manager.getMaxEntityId()).isEqualTo(3); // entity 2 still alive

        manager.destroyEntity(2);
        // Trail: 2 dead, 1 dead → shrinks to 1
        assertThat(manager.getMaxEntityId()).isEqualTo(1);
        assertThat(manager.exists(a)).isTrue();
    }

    @Test
    void maxEntityIdShrinksToZeroWhenAllDestroyed() {
        int a = manager.createEntity();
        int b = manager.createEntity();
        manager.destroyEntity(b);
        manager.destroyEntity(a);

        assertThat(manager.getMaxEntityId()).isZero();
    }

    @Test
    void maxEntityIdDoesNotShrinkWhenMiddleEntityDestroyed() {
        manager.createEntity(); // 0
        int mid = manager.createEntity(); // 1
        manager.createEntity(); // 2

        manager.destroyEntity(mid);
        // Entity 2 is still alive, so high-water mark stays at 3
        assertThat(manager.getMaxEntityId()).isEqualTo(3);
    }

    @Test
    void maxEntityIdGrowsAgainAfterRecycledCreate() {
        manager.createEntity(); // 0
        int b = manager.createEntity(); // 1
        manager.destroyEntity(b); // maxAliveId shrinks to 1
        assertThat(manager.getMaxEntityId()).isEqualTo(1);

        int recycled = manager.createEntity(); // recycles 1
        assertThat(recycled).isEqualTo(b);
        assertThat(manager.getMaxEntityId()).isEqualTo(2);
    }

    @Test
    void doubleDestroyDoesNotCorruptFreeList() {
        int id = manager.createEntity();
        manager.destroyEntity(id);
        manager.destroyEntity(id); // no-op: already dead

        assertThat(manager.getFreeCount()).isEqualTo(1);
    }

    @Test
    void negativeIdDestroyDoesNotThrow() {
        manager.destroyEntity(-1);
        assertThat(manager.getFreeCount()).isZero();
    }

    @Test
    void existsReturnsFalseForNegativeId() {
        assertThat(manager.exists(-1)).isFalse();
    }

    // ── Dense active array tests ──

    @Test
    void activeCountTracksLiveEntities() {
        assertThat(manager.getActiveCount()).isZero();

        manager.createEntity();
        assertThat(manager.getActiveCount()).isEqualTo(1);

        manager.createEntity();
        assertThat(manager.getActiveCount()).isEqualTo(2);
    }

    @Test
    void activeCountDecrementsOnDestroy() {
        int a = manager.createEntity();
        int b = manager.createEntity();
        assertThat(manager.getActiveCount()).isEqualTo(2);

        manager.destroyEntity(a);
        assertThat(manager.getActiveCount()).isEqualTo(1);

        manager.destroyEntity(b);
        assertThat(manager.getActiveCount()).isZero();
    }

    @Test
    void activeEntitiesContainsAllLiveIds() {
        int a = manager.createEntity();
        int b = manager.createEntity();
        int c = manager.createEntity();

        int[] active = manager.getActiveEntities();
        int count = manager.getActiveCount();

        assertThat(count).isEqualTo(3);
        assertThat(active[0]).isEqualTo(a);
        assertThat(active[1]).isEqualTo(b);
        assertThat(active[2]).isEqualTo(c);
    }

    @Test
    void swapAndPopPreservesActiveEntitiesAfterDestroy() {
        int a = manager.createEntity(); // 0
        int b = manager.createEntity(); // 1
        int c = manager.createEntity(); // 2

        // Destroying b (middle) should swap c into b's slot
        manager.destroyEntity(b);

        int[] active = manager.getActiveEntities();
        int count = manager.getActiveCount();
        assertThat(count).isEqualTo(2);

        // The dense array should contain {a, c} in some order
        java.util.Set<Integer> activeSet = new java.util.HashSet<>();
        for (int j = 0; j < count; j++) {
            activeSet.add(active[j]);
        }
        assertThat(activeSet).containsExactlyInAnyOrder(a, c);
    }

    @Test
    void activeCountRestoredAfterRecycle() {
        int a = manager.createEntity();
        manager.destroyEntity(a);
        assertThat(manager.getActiveCount()).isZero();

        int recycled = manager.createEntity();
        assertThat(manager.getActiveCount()).isEqualTo(1);
        assertThat(recycled).isEqualTo(a);

        int[] active = manager.getActiveEntities();
        assertThat(active[0]).isEqualTo(recycled);
    }

    @Test
    void doubleDestroyDoesNotCorruptActiveArray() {
        int a = manager.createEntity();
        int b = manager.createEntity();
        manager.destroyEntity(a);
        manager.destroyEntity(a); // no-op

        assertThat(manager.getActiveCount()).isEqualTo(1);
        assertThat(manager.getActiveEntities()[0]).isEqualTo(b);
    }
}
