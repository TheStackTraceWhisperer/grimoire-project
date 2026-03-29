package com.grimoire.domain.combat.component;

import com.grimoire.domain.core.component.Component;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AttackIntentTest {

    @Test
    void creation() {
        var intent = new AttackIntent("target-42");

        assertThat(intent.targetEntityId()).isEqualTo("target-42");
    }

    @Test
    void implementsComponent() {
        assertThat(new AttackIntent("x")).isInstanceOf(Component.class);
    }
}
