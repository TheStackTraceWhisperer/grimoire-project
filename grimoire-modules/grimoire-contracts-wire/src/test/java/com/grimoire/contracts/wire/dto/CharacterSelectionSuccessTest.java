package com.grimoire.contracts.wire.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CharacterSelectionSuccessTest {

    @Test
    void creationPreservesFields() {
        var success = new CharacterSelectionSuccess("ent-1", "Hero", 10, "zone-1", 5.0, 3.0);

        assertThat(success.entityId()).isEqualTo("ent-1");
        assertThat(success.characterName()).isEqualTo("Hero");
        assertThat(success.level()).isEqualTo(10);
        assertThat(success.zone()).isEqualTo("zone-1");
        assertThat(success.x()).isEqualTo(5.0);
        assertThat(success.y()).isEqualTo(3.0);
    }
}
