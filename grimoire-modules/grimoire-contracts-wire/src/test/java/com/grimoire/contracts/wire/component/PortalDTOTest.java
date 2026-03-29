package com.grimoire.contracts.wire.component;

import com.grimoire.contracts.api.component.ComponentDTO;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PortalDTOTest {

    @Test
    void creationPreservesFields() {
        var portal = new PortalDTO(3.0, 5.0);

        assertThat(portal.width()).isEqualTo(3.0);
        assertThat(portal.height()).isEqualTo(5.0);
    }

    @Test
    void implementsComponentDTO() {
        assertThat(new PortalDTO(0, 0)).isInstanceOf(ComponentDTO.class);
    }
}
