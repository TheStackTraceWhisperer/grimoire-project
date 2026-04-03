package com.grimoire.infra.observability;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link Markers} verifying all marker constants are well-defined.
 */
class MarkersTest {

    @Test
    void networkMarkerHasCorrectName() {
        assertThat(Markers.NETWORK).isNotNull();
        assertThat(Markers.NETWORK.getName()).isEqualTo("NETWORK");
    }

    @Test
    void sessionMarkerHasCorrectName() {
        assertThat(Markers.SESSION).isNotNull();
        assertThat(Markers.SESSION.getName()).isEqualTo("SESSION");
    }

    @Test
    void combatMarkerHasCorrectName() {
        assertThat(Markers.COMBAT).isNotNull();
        assertThat(Markers.COMBAT.getName()).isEqualTo("COMBAT");
    }

    @Test
    void persistenceMarkerHasCorrectName() {
        assertThat(Markers.PERSISTENCE).isNotNull();
        assertThat(Markers.PERSISTENCE.getName()).isEqualTo("PERSISTENCE");
    }

    @Test
    void engineMarkerHasCorrectName() {
        assertThat(Markers.ENGINE).isNotNull();
        assertThat(Markers.ENGINE.getName()).isEqualTo("ENGINE");
    }

    @Test
    void navigationMarkerHasCorrectName() {
        assertThat(Markers.NAVIGATION).isNotNull();
        assertThat(Markers.NAVIGATION.getName()).isEqualTo("NAVIGATION");
    }
}
