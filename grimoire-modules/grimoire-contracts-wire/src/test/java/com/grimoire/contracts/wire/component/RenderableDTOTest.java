package com.grimoire.contracts.wire.component;

import com.grimoire.contracts.api.component.ComponentDTO;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RenderableDTOTest {

    @Test
    void creationPreservesFields() {
        var renderable = new RenderableDTO("Player", "visual-player");

        assertThat(renderable.name()).isEqualTo("Player");
        assertThat(renderable.visualId()).isEqualTo("visual-player");
    }

    @Test
    void implementsComponentDTO() {
        assertThat(new RenderableDTO("Test", "visual-test")).isInstanceOf(ComponentDTO.class);
    }

    @Test
    void implementsSerializable() {
        assertThat(new RenderableDTO("NPC", "visual-npc")).isInstanceOf(java.io.Serializable.class);
    }
}
