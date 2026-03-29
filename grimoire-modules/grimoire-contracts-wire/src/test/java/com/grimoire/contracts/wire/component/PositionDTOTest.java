package com.grimoire.contracts.wire.component;

import com.grimoire.contracts.api.component.ComponentDTO;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PositionDTOTest {

    @Test
    void creationPreservesFields() {
        var pos = new PositionDTO(10.5, 20.3);

        assertThat(pos.x()).isEqualTo(10.5);
        assertThat(pos.y()).isEqualTo(20.3);
    }

    @Test
    void implementsComponentDTO() {
        assertThat(new PositionDTO(0, 0)).isInstanceOf(ComponentDTO.class);
    }

    @Test
    void implementsSerializable() {
        assertThat(new PositionDTO(0, 0)).isInstanceOf(java.io.Serializable.class);
    }
}
