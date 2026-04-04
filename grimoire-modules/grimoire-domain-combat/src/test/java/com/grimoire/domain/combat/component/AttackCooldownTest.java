package com.grimoire.domain.combat.component;

import com.grimoire.domain.core.component.Component;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AttackCooldownTest {

    @Test
    void creation() {
        var cd = new AttackCooldown(20);

        assertThat(cd.ticksRemaining).isEqualTo(20);
    }

    @Test
    void noArgConstructor() {
        var cd = new AttackCooldown();

        assertThat(cd.ticksRemaining).isZero();
    }

    @Test
    void zeroTicks() {
        assertThat(new AttackCooldown(0).ticksRemaining).isZero();
    }

    @Test
    void implementsComponent() {
        assertThat(new AttackCooldown(1)).isInstanceOf(Component.class);
    }

    @Test
    void decrementReducesTicks() {
        var a = new AttackCooldown(20);
        int result = a.decrement();

        assertThat(a.ticksRemaining).isEqualTo(19);
        assertThat(result).isEqualTo(19);
    }

    @Test
    void updateSetsNewValue() {
        var cd = new AttackCooldown(10);

        cd.update(50);

        assertThat(cd.ticksRemaining).isEqualTo(50);
    }

    @Test
    void equalsSameValues() {
        assertThat(new AttackCooldown(10)).isEqualTo(new AttackCooldown(10));
    }

    @Test
    void equalsSameInstance() {
        var cd = new AttackCooldown(5);

        assertThat(cd).isEqualTo(cd);
    }

    @Test
    void notEqualsDifferentValues() {
        assertThat(new AttackCooldown(10)).isNotEqualTo(new AttackCooldown(20));
    }

    @Test
    void notEqualsDifferentType() {
        assertThat(new AttackCooldown(10)).isNotEqualTo("not a cooldown");
    }

    @Test
    void hashCodeConsistent() {
        assertThat(new AttackCooldown(10).hashCode())
                .isEqualTo(new AttackCooldown(10).hashCode());
    }

    @Test
    void toStringContainsValue() {
        assertThat(new AttackCooldown(15).toString()).contains("15");
    }
}
