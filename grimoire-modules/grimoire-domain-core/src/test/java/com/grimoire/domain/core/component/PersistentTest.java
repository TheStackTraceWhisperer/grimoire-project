package com.grimoire.domain.core.component;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PersistentTest {

    @Test
    void creation() {
        var p = new Persistent("keycloak-sub-12345");

        assertThat(p.accountId).isEqualTo("keycloak-sub-12345");
    }

    @Test
    void implementsComponent() {
        assertThat(new Persistent("a")).isInstanceOf(Component.class);
    }

    @Test
    void noArgConstructor() {
        var p = new Persistent();
        assertThat(p.accountId).isNull();
    }

    @Test
    void update() {
        var p = new Persistent("old");
        p.update("new-id");
        assertThat(p.accountId).isEqualTo("new-id");
    }

    @Test
    void equality() {
        assertThat(new Persistent("a")).isEqualTo(new Persistent("a"));
        assertThat(new Persistent("a")).isNotEqualTo(new Persistent("b"));
        assertThat(new Persistent("a")).isNotEqualTo(null);
    }

    @Test
    void toStringFormat() {
        assertThat(new Persistent("acc-1").toString()).contains("acc-1");
    }

    @Test
    void hashCodeConsistency() {
        assertThat(new Persistent("a").hashCode()).isEqualTo(new Persistent("a").hashCode());
    }

    @Test
    void hashCodeNullAccountId() {
        assertThat(new Persistent().hashCode()).isZero();
    }
}
