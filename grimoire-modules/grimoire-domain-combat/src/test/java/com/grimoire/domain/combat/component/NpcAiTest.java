package com.grimoire.domain.combat.component;

import com.grimoire.domain.combat.component.NpcAi.AiType;
import com.grimoire.domain.core.component.Component;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NpcAiTest {

    @Test
    void creation() {
        var ai = new NpcAi(AiType.HOSTILE_AGGRO_MELEE);

        assertThat(ai.type()).isEqualTo(AiType.HOSTILE_AGGRO_MELEE);
    }

    @Test
    void implementsComponent() {
        assertThat(new NpcAi(AiType.FRIENDLY_WANDER)).isInstanceOf(Component.class);
    }

    @Test
    void aiTypeCount() {
        assertThat(AiType.values()).hasSize(2);
    }
}
