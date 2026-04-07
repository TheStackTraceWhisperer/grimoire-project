package com.grimoire.application.core.ecs;

import java.util.Arrays;

/**
 * Manages entity lifecycle with primitive int IDs, a free-list for recycling,
 * and a dense active-entity array for cache-friendly iteration.
 *
 * <p>
 * Entities are primitive ints. Destroyed IDs are returned to a free-list and
 * recycled by subsequent {@link #createEntity()} calls. A dense
 * {@code activeEntities} array enables O(1) swap-and-pop removal and tight
 * system iteration without scanning dead slots.
 * </p>
 *
 * <p>
 * <strong>Thread-safety:</strong> This class must only be accessed from the
 * single-threaded game loop. Register as a singleton at the assembly layer.
 * </p>
 */
public class EntityManager {

    /** Maximum number of concurrent entities. */
    public static final int MAX_ENTITIES = 100_000;

    /** Initial capacity of the free-list (grows as needed). */
    private static final int FREE_LIST_INITIAL_CAPACITY = 1024;

    /** Alive flags indexed by entity ID. */
    private final boolean[] alive = new boolean[MAX_ENTITIES];

    /**
     * Dense array of active entity IDs for cache-friendly iteration. Only indices
     * {@code [0, activeCount)} contain valid IDs.
     */
    private final int[] activeEntities = new int[MAX_ENTITIES];

    /**
     * Reverse-lookup: maps entity ID → index in {@code activeEntities}. Only valid
     * when {@code alive[entityId]} is true.
     */
    private final int[] entityToActiveIndex = new int[MAX_ENTITIES];

    /** Number of currently active entities. */
    private int activeCount;

    /**
     * Free-list of recycled entity IDs. IDs are pushed on destroy and popped on
     * create, giving O(1) recycling.
     */
    private int[] freeList = new int[FREE_LIST_INITIAL_CAPACITY];

    /** Number of IDs currently in the free-list. */
    private int freeCount;

    /** Next fresh (never-used) entity ID — only used when free-list is empty. */
    private int nextFreshId;

    /**
     * High-water mark for active entity IDs (exclusive upper bound for iteration).
     * Shrunk when trailing entities are destroyed.
     */
    private int maxAliveId;

    /**
     * Creates a new entity with a unique int ID.
     *
     * <p>
     * Recycles a previously destroyed ID if available, otherwise allocates a fresh
     * one. The entity is appended to the dense active-entity array.
     * </p>
     *
     * @return the entity ID
     * @throws IllegalStateException
     *             if the entity limit is exceeded
     */
    public int createEntity() {
        int entityId;
        if (freeCount > 0) {
            freeCount--;
            entityId = freeList[freeCount];
        } else {
            entityId = nextFreshId;
            nextFreshId++;
            if (entityId >= MAX_ENTITIES) {
                throw new IllegalStateException("Entity limit exceeded: " + MAX_ENTITIES);
            }
        }
        alive[entityId] = true;
        if (entityId >= maxAliveId) {
            maxAliveId = entityId + 1;
        }

        // Append to dense active array
        int idx = activeCount;
        activeEntities[idx] = entityId;
        entityToActiveIndex[entityId] = idx;
        activeCount++;

        return entityId;
    }

    /**
     * Destroys an entity and removes it from the dense active array using O(1)
     * swap-and-pop.
     *
     * <p>
     * If the destroyed entity is at the tail of the alive range, the high-water
     * mark is shrunk so legacy iteration scans fewer slots.
     * </p>
     *
     * @param entityId
     *            the entity ID
     */
    public void destroyEntity(int entityId) {
        if (entityId < 0 || entityId >= MAX_ENTITIES || !alive[entityId]) {
            return;
        }
        alive[entityId] = false;

        // Swap-and-pop from dense active array
        int idx = entityToActiveIndex[entityId];
        int lastIdx = activeCount - 1;
        if (idx != lastIdx) {
            int lastEntity = activeEntities[lastIdx];
            activeEntities[idx] = lastEntity;
            entityToActiveIndex[lastEntity] = idx;
        }
        activeCount--;

        // Push onto free-list (grow if needed)
        if (freeCount == freeList.length) {
            freeList = Arrays.copyOf(freeList, freeList.length * 2);
        }
        freeList[freeCount] = entityId;
        freeCount++;

        // Shrink high-water mark if we destroyed the trailing entity
        if (entityId == maxAliveId - 1) {
            while (maxAliveId > 0 && !alive[maxAliveId - 1]) {
                maxAliveId--;
            }
        }
    }

    /**
     * Checks if an entity exists.
     *
     * @param entityId
     *            the entity ID
     * @return true if the entity exists
     */
    public boolean exists(int entityId) {
        return entityId >= 0 && entityId < MAX_ENTITIES && alive[entityId];
    }

    /**
     * Returns the high-water mark for iteration.
     *
     * @return the exclusive upper bound of active entity IDs
     */
    public int getMaxEntityId() {
        return maxAliveId;
    }

    /**
     * Returns the alive array for direct iteration by systems.
     *
     * @return the alive flags
     */
    public boolean[] getAlive() {
        return alive;
    }

    /**
     * Returns the dense array of active entity IDs.
     *
     * <p>
     * Only indices {@code [0, getActiveCount())} contain valid IDs.
     * </p>
     *
     * @return the active entity ID array
     */
    public int[] getActiveEntities() {
        return activeEntities;
    }

    /**
     * Returns the number of currently active entities.
     *
     * @return active entity count
     */
    public int getActiveCount() {
        return activeCount;
    }

    /**
     * Returns the number of IDs currently available for recycling.
     *
     * @return free-list size
     */
    public int getFreeCount() {
        return freeCount;
    }
}
