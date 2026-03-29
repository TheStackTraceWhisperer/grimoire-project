package com.grimoire.domain.core.component;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PortalCooldownTest {

    @Test
    void creation() {
        var cd = new PortalCooldown(60);

        assertThat(cd.ticksRemaining()).isEqualTo(60);
    }

    @Test
    void implementsComponent() {
        assertThat(new PortalCooldown(0)).isInstanceOf(Component.class);
    }
}
