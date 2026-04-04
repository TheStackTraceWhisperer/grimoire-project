package com.grimoire.domain.combat.component;

import com.grimoire.domain.core.component.Component;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AttackIntentTest {

    @Test
    void creation() {
        var intent = new AttackIntent(42);

        assertThat(intent.targetEntityId).isEqualTo(42);
    }

    @Test
    void noArgConstructorDefaultsToMinusOne() {
        var intent = new AttackIntent();

        assertThat(intent.targetEntityId).isEqualTo(-1);
    }

    @Test
    void implementsComponent() {
        assertThat(new AttackIntent(0)).isInstanceOf(Component.class);
    }

    @Test
    void updateSetsNewTarget() {
        var intent = new AttackIntent(1);

        intent.update(99);

        assertThat(intent.targetEntityId).isEqualTo(99);
    }

    @Test
    void equalsSameValues() {
        assertThat(new AttackIntent(42)).isEqualTo(new AttackIntent(42));
    }

    @Test
    void equalsSameInstance() {
        var intent = new AttackIntent(5);

        assertThat(intent).isEqualTo(intent);
    }

    @Test
    void notEqualsDifferentValues() {
        assertThat(new AttackIntent(1)).isNotEqualTo(new AttackIntent(2));
    }

    @Test
    void notEqualsDifferentType() {
        assertThat(new AttackIntent(1)).isNotEqualTo("not an intent");
    }

    @Test
    void hashCodeConsistent() {
        assertThat(new AttackIntent(42).hashCode())
                .isEqualTo(new AttackIntent(42).hashCode());
    }

    @Test
    void toStringContainsTarget() {
        assertThat(new AttackIntent(42).toString()).contains("42");
    }
}
