package com.grimoire.domain.combat.component;

import com.grimoire.domain.core.component.Component;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AttackCooldownTest {

    @Test
    void creation() {
        var cd = new AttackCooldown(20);

        assertThat(cd.ticksRemaining()).isEqualTo(20);
    }

    @Test
    void zeroTicks() {
        assertThat(new AttackCooldown(0).ticksRemaining()).isZero();
    }

    @Test
    void implementsComponent() {
        assertThat(new AttackCooldown(1)).isInstanceOf(Component.class);
    }

    @Test
    void immutability() {
        var a = new AttackCooldown(20);
        var b = new AttackCooldown(19);

        assertThat(a).isNotEqualTo(b);
        assertThat(a.ticksRemaining()).isEqualTo(20);
    }
}
