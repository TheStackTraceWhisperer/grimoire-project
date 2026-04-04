package com.grimoire.application.core.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * In-memory group management service.
 *
 * <p>
 * Manages player groups with create / join / leave / disband operations. Each
 * group is identified by a generated ID and tracks member entity IDs (ints). An
 * entity may belong to at most one group at a time.
 * </p>
 *
 * <p>
 * <strong>Thread-safety:</strong> This class is not thread-safe. It must be
 * accessed from the game loop thread or protected externally.
 * </p>
 */
public class GroupService {

    /** Group ID → member entity IDs. */
    private final Map<String, Set<Integer>> groups = new HashMap<>();

    /** Entity ID → group ID (reverse index). */
    private final Map<Integer, String> entityToGroup = new HashMap<>();

    /**
     * Creates a new group with the given entity as its first member.
     *
     * @param entityId
     *            the creating entity
     * @return the generated group ID
     */
    public String createGroup(int entityId) {
        leaveCurrentGroup(entityId);

        String groupId = UUID.randomUUID().toString();
        Set<Integer> members = new HashSet<>();
        members.add(entityId);
        groups.put(groupId, members);
        entityToGroup.put(entityId, groupId);
        return groupId;
    }

    /**
     * Adds an entity to an existing group.
     *
     * @param groupId
     *            the group to join
     * @param entityId
     *            the entity joining
     * @return {@code true} if the entity was added
     */
    public boolean joinGroup(String groupId, int entityId) {
        java.util.Objects.requireNonNull(groupId, "groupId must not be null");

        Set<Integer> members = groups.get(groupId);
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
     * @param entityId
     *            the entity leaving
     * @return {@code true} if the entity was in a group and was removed
     */
    public boolean leaveGroup(int entityId) {
        return leaveCurrentGroup(entityId);
    }

    /**
     * Returns the members of a group.
     *
     * @param groupId
     *            the group ID
     * @return unmodifiable set of member entity IDs
     */
    public Set<Integer> getMembers(String groupId) {
        Set<Integer> members = groups.get(groupId);
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
    public Optional<String> getGroupForEntity(int entityId) {
        return Optional.ofNullable(entityToGroup.get(entityId));
    }

    /** Returns the number of active groups. */
    public int getGroupCount() {
        return groups.size();
    }

    /** Checks whether a group exists. */
    public boolean groupExists(String groupId) {
        return groups.containsKey(groupId);
    }

    private boolean leaveCurrentGroup(int entityId) {
        String currentGroupId = entityToGroup.remove(entityId);
        if (currentGroupId == null) {
            return false;
        }
        Set<Integer> members = groups.get(currentGroupId);
        if (members != null) {
            members.remove(entityId);
            if (members.isEmpty()) {
                groups.remove(currentGroupId);
            }
        }
        return true;
    }
}
