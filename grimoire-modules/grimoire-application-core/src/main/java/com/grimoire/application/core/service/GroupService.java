package com.grimoire.application.core.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * In-memory group management service.
 *
 * <p>
 * Manages player groups with create / join / leave / disband operations. Each
 * group is identified by a generated ID and tracks member entity IDs. An entity
 * may belong to at most one group at a time.
 * </p>
 *
 * <p>
 * <strong>Thread-safety:</strong> This class is not thread-safe. It must be
 * accessed from the game loop thread or protected externally.
 * </p>
 */
public class GroupService {

    /** Group ID → member entity IDs. */
    @SuppressWarnings("PMD.UseConcurrentHashMap")
    private final Map<String, Set<String>> groups = new HashMap<>();

    /** Entity ID → group ID (reverse index). */
    @SuppressWarnings("PMD.UseConcurrentHashMap")
    private final Map<String, String> entityToGroup = new HashMap<>();

    /**
     * Creates a new group with the given entity as its first member.
     *
     * <p>
     * If the entity is already in a group, it is removed from the old group first.
     * </p>
     *
     * @param entityId
     *            the creating entity
     * @return the generated group ID
     */
    public String createGroup(String entityId) {
        Objects.requireNonNull(entityId, "entityId must not be null");

        leaveCurrentGroup(entityId);

        String groupId = UUID.randomUUID().toString();
        Set<String> members = new HashSet<>();
        members.add(entityId);
        groups.put(groupId, members);
        entityToGroup.put(entityId, groupId);
        return groupId;
    }

    /**
     * Adds an entity to an existing group.
     *
     * <p>
     * If the entity is already in another group, it is removed from the old group
     * first.
     * </p>
     *
     * @param groupId
     *            the group to join
     * @param entityId
     *            the entity joining
     * @return {@code true} if the entity was added, {@code false} if the group does
     *         not exist
     */
    public boolean joinGroup(String groupId, String entityId) {
        Objects.requireNonNull(groupId, "groupId must not be null");
        Objects.requireNonNull(entityId, "entityId must not be null");

        Set<String> members = groups.get(groupId);
        if (members == null) {
            return false;
        }

        leaveCurrentGroup(entityId);
        members.add(entityId);
        entityToGroup.put(entityId, groupId);
        return true;
    }

    /**
     * Removes an entity from its current group.
     *
     * <p>
     * If the group becomes empty after removal, it is automatically disbanded.
     * </p>
     *
     * @param entityId
     *            the entity leaving
     * @return {@code true} if the entity was in a group and was removed
     */
    public boolean leaveGroup(String entityId) {
        Objects.requireNonNull(entityId, "entityId must not be null");
        return leaveCurrentGroup(entityId);
    }

    /**
     * Returns the members of a group.
     *
     * @param groupId
     *            the group ID
     * @return unmodifiable set of member entity IDs, or empty set if the group does
     *         not exist
     */
    public Set<String> getMembers(String groupId) {
        Set<String> members = groups.get(groupId);
        if (members == null) {
            return Set.of();
        }
        return Collections.unmodifiableSet(members);
    }

    /**
     * Returns the group ID for an entity, if it belongs to one.
     *
     * @param entityId
     *            the entity ID
     * @return the group ID, or empty
     */
    public Optional<String> getGroupForEntity(String entityId) {
        return Optional.ofNullable(entityToGroup.get(entityId));
    }

    /**
     * Returns the number of active groups.
     *
     * @return group count
     */
    public int getGroupCount() {
        return groups.size();
    }

    /**
     * Checks whether a group exists.
     *
     * @param groupId
     *            the group ID
     * @return {@code true} if the group exists
     */
    public boolean groupExists(String groupId) {
        return groups.containsKey(groupId);
    }

    private boolean leaveCurrentGroup(String entityId) {
        String currentGroupId = entityToGroup.remove(entityId);
        if (currentGroupId == null) {
            return false;
        }
        Set<String> members = groups.get(currentGroupId);
        if (members != null) {
            members.remove(entityId);
            if (members.isEmpty()) {
                groups.remove(currentGroupId);
            }
        }
        return true;
    }
}
