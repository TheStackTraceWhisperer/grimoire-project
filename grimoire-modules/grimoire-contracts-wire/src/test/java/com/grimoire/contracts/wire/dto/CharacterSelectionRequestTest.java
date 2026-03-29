package com.grimoire.contracts.wire.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CharacterSelectionRequestTest {

    @Test
    void creationPreservesFields() {
        var req = new CharacterSelectionRequest(7L);
        assertThat(req.characterId()).isEqualTo(7L);
    }
}
