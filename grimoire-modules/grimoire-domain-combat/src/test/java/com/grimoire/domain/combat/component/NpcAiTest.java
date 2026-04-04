package com.grimoire.domain.combat.component;

import com.grimoire.domain.combat.component.NpcAi.AiType;
import com.grimoire.domain.core.component.Component;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NpcAiTest {

    @Test
    void creation() {
        var ai = new NpcAi(AiType.HOSTILE_AGGRO_MELEE);

        assertThat(ai.type).isEqualTo(AiType.HOSTILE_AGGRO_MELEE);
    }

    @Test
    void noArgConstructor() {
        var ai = new NpcAi();

        assertThat(ai.type).isNull();
    }

    @Test
    void implementsComponent() {
        assertThat(new NpcAi(AiType.FRIENDLY_WANDER)).isInstanceOf(Component.class);
    }

    @Test
    void aiTypeCount() {
        assertThat(AiType.values()).hasSize(2);
    }

    @Test
    void updateChangesType() {
        var ai = new NpcAi(AiType.FRIENDLY_WANDER);

        ai.update(AiType.HOSTILE_AGGRO_MELEE);

        assertThat(ai.type).isEqualTo(AiType.HOSTILE_AGGRO_MELEE);
    }

    @Test
    void equalsSameValues() {
        assertThat(new NpcAi(AiType.FRIENDLY_WANDER))
                .isEqualTo(new NpcAi(AiType.FRIENDLY_WANDER));
    }

    @Test
    void equalsSameInstance() {
        var ai = new NpcAi(AiType.HOSTILE_AGGRO_MELEE);

        assertThat(ai).isEqualTo(ai);
    }

    @Test
    void notEqualsDifferentType() {
        assertThat(new NpcAi(AiType.FRIENDLY_WANDER))
                .isNotEqualTo(new NpcAi(AiType.HOSTILE_AGGRO_MELEE));
    }

    @Test
    void notEqualsDifferentObjectType() {
        assertThat(new NpcAi(AiType.FRIENDLY_WANDER)).isNotEqualTo("not an ai");
    }

    @Test
    void hashCodeConsistent() {
        assertThat(new NpcAi(AiType.HOSTILE_AGGRO_MELEE).hashCode())
                .isEqualTo(new NpcAi(AiType.HOSTILE_AGGRO_MELEE).hashCode());
    }

    @Test
    void hashCodeWithNullType() {
        var ai = new NpcAi();

        assertThat(ai.hashCode()).isEqualTo(0);
    }

    @Test
    void toStringContainsType() {
        assertThat(new NpcAi(AiType.HOSTILE_AGGRO_MELEE).toString())
                .contains("HOSTILE_AGGRO_MELEE");
    }
}
