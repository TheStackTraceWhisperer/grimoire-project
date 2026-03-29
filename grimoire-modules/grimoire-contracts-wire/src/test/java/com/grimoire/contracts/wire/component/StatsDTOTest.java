package com.grimoire.contracts.wire.component;

import com.grimoire.contracts.api.component.ComponentDTO;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StatsDTOTest {

    @Test
    void creationPreservesFields() {
        var stats = new StatsDTO(80, 100);

        assertThat(stats.currentHp()).isEqualTo(80);
        assertThat(stats.maxHp()).isEqualTo(100);
    }

    @Test
    void implementsComponentDTO() {
        assertThat(new StatsDTO(0, 0)).isInstanceOf(ComponentDTO.class);
    }
}
