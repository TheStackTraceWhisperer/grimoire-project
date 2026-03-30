package com.grimoire.application.core.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GroupServiceTest {

    private GroupService service;

    @BeforeEach
    void setUp() {
        service = new GroupService();
    }

    @Test
    void createGroupReturnsGroupId() {
        String groupId = service.createGroup("entity-1");
        assertThat(groupId).isNotNull().isNotEmpty();
    }

    @Test
    void creatorIsMemberOfNewGroup() {
        String groupId = service.createGroup("entity-1");
        Set<String> members = service.getMembers(groupId);
        assertThat(members).containsExactly("entity-1");
    }

    @Test
    void joinGroupAddsMember() {
        String groupId = service.createGroup("entity-1");
        boolean joined = service.joinGroup(groupId, "entity-2");
        assertThat(joined).isTrue();
        assertThat(service.getMembers(groupId)).containsExactlyInAnyOrder("entity-1", "entity-2");
    }

    @Test
    void joinNonExistentGroupReturnsFalse() {
        boolean joined = service.joinGroup("nonexistent", "entity-1");
        assertThat(joined).isFalse();
    }

    @Test
    void leaveGroupRemovesMember() {
        String groupId = service.createGroup("entity-1");
        service.joinGroup(groupId, "entity-2");

        boolean left = service.leaveGroup("entity-1");
        assertThat(left).isTrue();
        assertThat(service.getMembers(groupId)).containsExactly("entity-2");
    }

    @Test
    void leaveGroupReturnsFalseWhenNotInGroup() {
        boolean left = service.leaveGroup("entity-1");
        assertThat(left).isFalse();
    }

    @Test
    void groupDisbandedWhenLastMemberLeaves() {
        String groupId = service.createGroup("entity-1");

        service.leaveGroup("entity-1");

        assertThat(service.groupExists(groupId)).isFalse();
        assertThat(service.getMembers(groupId)).isEmpty();
    }

    @Test
    void getGroupForEntityReturnsGroupId() {
        String groupId = service.createGroup("entity-1");
        Optional<String> result = service.getGroupForEntity("entity-1");
        assertThat(result).contains(groupId);
    }

    @Test
    void getGroupForEntityReturnsEmptyWhenNotInGroup() {
        Optional<String> result = service.getGroupForEntity("entity-1");
        assertThat(result).isEmpty();
    }

    @Test
    void creatingGroupLeavesOldGroup() {
        String group1 = service.createGroup("entity-1");
        String group2 = service.createGroup("entity-1");

        assertThat(service.groupExists(group1)).isFalse();
        assertThat(service.getGroupForEntity("entity-1")).contains(group2);
    }

    @Test
    void joiningGroupLeavesOldGroup() {
        String group1 = service.createGroup("entity-1");
        String group2 = service.createGroup("entity-2");

        service.joinGroup(group2, "entity-1");

        assertThat(service.groupExists(group1)).isFalse();
        assertThat(service.getGroupForEntity("entity-1")).contains(group2);
    }

    @Test
    void getGroupCountReturnsCorrectCount() {
        assertThat(service.getGroupCount()).isZero();

        service.createGroup("entity-1");
        assertThat(service.getGroupCount()).isEqualTo(1);

        service.createGroup("entity-2");
        assertThat(service.getGroupCount()).isEqualTo(2);
    }

    @Test
    void groupExistsReturnsTrueForExistingGroup() {
        String groupId = service.createGroup("entity-1");
        assertThat(service.groupExists(groupId)).isTrue();
    }

    @Test
    void groupExistsReturnsFalseForNonExistentGroup() {
        assertThat(service.groupExists("nonexistent")).isFalse();
    }

    @Test
    void getMembersReturnsEmptyForNonExistentGroup() {
        assertThat(service.getMembers("nonexistent")).isEmpty();
    }

    @Test
    void membersSetIsUnmodifiable() {
        String groupId = service.createGroup("entity-1");
        Set<String> members = service.getMembers(groupId);
        assertThrows(
                UnsupportedOperationException.class,
                () -> members.add("hacker"));
    }
}
