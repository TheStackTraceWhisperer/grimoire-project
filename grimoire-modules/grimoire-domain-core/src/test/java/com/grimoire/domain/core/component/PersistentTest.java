package com.grimoire.domain.core.component;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PersistentTest {

    @Test
    void creation() {
        var p = new Persistent("keycloak-sub-12345");

        assertThat(p.accountId()).isEqualTo("keycloak-sub-12345");
    }

    @Test
    void implementsComponent() {
        assertThat(new Persistent("a")).isInstanceOf(Component.class);
    }
}
