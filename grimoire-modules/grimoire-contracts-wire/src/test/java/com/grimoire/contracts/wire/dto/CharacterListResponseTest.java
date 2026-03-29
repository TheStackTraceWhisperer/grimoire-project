package com.grimoire.contracts.wire.dto;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CharacterListResponseTest {

    @Test
    void creationPreservesFields() {
        var info = new CharacterListResponse.CharacterInfo(1L, "Hero", 10, "zone-1");
        var response = new CharacterListResponse("session-1", List.of(info));

        assertThat(response.sessionId()).isEqualTo("session-1");
        assertThat(response.characters()).hasSize(1);
        assertThat(response.characters().getFirst().name()).isEqualTo("Hero");
    }

    @Test
    void characterInfoPreservesFields() {
        var info = new CharacterListResponse.CharacterInfo(42L, "Mage", 25, "dungeon-3");

        assertThat(info.id()).isEqualTo(42L);
        assertThat(info.name()).isEqualTo("Mage");
        assertThat(info.level()).isEqualTo(25);
        assertThat(info.lastZone()).isEqualTo("dungeon-3");
    }

    @Test
    void nullCharacterListDefaultsToEmpty() {
        var response = new CharacterListResponse("s1", null);
        assertThat(response.characters()).isEmpty();
    }

    @Test
    void characterListIsUnmodifiable() {
        var response = new CharacterListResponse("s1", List.of());
        assertThat(response.characters()).isUnmodifiable();
    }
}
