package com.grimoire.domain.core.component;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PortalCooldownTest {

    @Test
    void creation() {
        var cd = new PortalCooldown(60);

        assertThat(cd.ticksRemaining).isEqualTo(60);
    }

    @Test
    void implementsComponent() {
        assertThat(new PortalCooldown(0)).isInstanceOf(Component.class);
    }

    @Test
    void noArgConstructor() {
        var cd = new PortalCooldown();
        assertThat(cd.ticksRemaining).isZero();
    }

    @Test
    void update() {
        var cd = new PortalCooldown(10);
        cd.update(50);
        assertThat(cd.ticksRemaining).isEqualTo(50);
    }

    @Test
    void decrement() {
        var cd = new PortalCooldown(5);
        assertThat(cd.decrement()).isEqualTo(4);
        assertThat(cd.ticksRemaining).isEqualTo(4);
    }

    @Test
    void equality() {
        assertThat(new PortalCooldown(10)).isEqualTo(new PortalCooldown(10));
        assertThat(new PortalCooldown(10)).isNotEqualTo(new PortalCooldown(20));
        assertThat(new PortalCooldown(10)).isNotEqualTo(null);
    }

    @Test
    void toStringFormat() {
        assertThat(new PortalCooldown(42).toString()).contains("42");
    }

    @Test
    void hashCodeConsistency() {
        assertThat(new PortalCooldown(10).hashCode()).isEqualTo(new PortalCooldown(10).hashCode());
    }
}
