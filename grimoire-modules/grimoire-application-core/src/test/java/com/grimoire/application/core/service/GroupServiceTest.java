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
        String groupId = service.createGroup(1);
        assertThat(groupId).isNotNull().isNotEmpty();
    }

    @Test
    void creatorIsMemberOfNewGroup() {
        String groupId = service.createGroup(1);
        Set<Integer> members = service.getMembers(groupId);
        assertThat(members).containsExactly(1);
    }

    @Test
    void joinGroupAddsMember() {
        String groupId = service.createGroup(1);
        boolean joined = service.joinGroup(groupId, 2);
        assertThat(joined).isTrue();
        assertThat(service.getMembers(groupId)).containsExactlyInAnyOrder(1, 2);
    }

    @Test
    void joinNonExistentGroupReturnsFalse() {
        boolean joined = service.joinGroup("nonexistent", 1);
        assertThat(joined).isFalse();
    }

    @Test
    void leaveGroupRemovesMember() {
        String groupId = service.createGroup(1);
        service.joinGroup(groupId, 2);

        boolean left = service.leaveGroup(1);
        assertThat(left).isTrue();
        assertThat(service.getMembers(groupId)).containsExactly(2);
    }

    @Test
    void leaveGroupReturnsFalseWhenNotInGroup() {
        boolean left = service.leaveGroup(1);
        assertThat(left).isFalse();
    }

    @Test
    void groupDisbandedWhenLastMemberLeaves() {
        String groupId = service.createGroup(1);

        service.leaveGroup(1);

        assertThat(service.groupExists(groupId)).isFalse();
        assertThat(service.getMembers(groupId)).isEmpty();
    }

    @Test
    void getGroupForEntityReturnsGroupId() {
        String groupId = service.createGroup(1);
        Optional<String> result = service.getGroupForEntity(1);
        assertThat(result).contains(groupId);
    }

    @Test
    void getGroupForEntityReturnsEmptyWhenNotInGroup() {
        Optional<String> result = service.getGroupForEntity(1);
        assertThat(result).isEmpty();
    }

    @Test
    void creatingGroupLeavesOldGroup() {
        String group1 = service.createGroup(1);
        String group2 = service.createGroup(1);

        assertThat(service.groupExists(group1)).isFalse();
        assertThat(service.getGroupForEntity(1)).contains(group2);
    }

    @Test
    void joiningGroupLeavesOldGroup() {
        String group1 = service.createGroup(1);
        String group2 = service.createGroup(2);

        service.joinGroup(group2, 1);

        assertThat(service.groupExists(group1)).isFalse();
        assertThat(service.getGroupForEntity(1)).contains(group2);
    }

    @Test
    void getGroupCountReturnsCorrectCount() {
        assertThat(service.getGroupCount()).isZero();

        service.createGroup(1);
        assertThat(service.getGroupCount()).isEqualTo(1);

        service.createGroup(2);
        assertThat(service.getGroupCount()).isEqualTo(2);
    }

    @Test
    void groupExistsReturnsTrueForExistingGroup() {
        String groupId = service.createGroup(1);
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
        String groupId = service.createGroup(1);
        Set<Integer> members = service.getMembers(groupId);
        assertThrows(
                UnsupportedOperationException.class,
                () -> members.add(999));
    }
}
