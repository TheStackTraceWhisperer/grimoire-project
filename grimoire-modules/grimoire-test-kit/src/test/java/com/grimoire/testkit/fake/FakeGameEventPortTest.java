package com.grimoire.testkit.fake;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link FakeGameEventPort}.
 */
class FakeGameEventPortTest {

    private FakeGameEventPort port;

    @BeforeEach
    void setUp() {
        port = new FakeGameEventPort();
    }

    @Test
    void startsWithNoEvents() {
        assertThat(port.totalEventCount()).isZero();
        assertThat(port.despawnEvents()).isEmpty();
        assertThat(port.zoneChangeEvents()).isEmpty();
    }

    @Test
    void recordsDespawnEvent() {
        port.onEntityDespawn("e1", "zone-a");

        assertThat(port.despawnEvents()).hasSize(1);
        assertThat(port.despawnEvents().getFirst().entityId()).isEqualTo("e1");
        assertThat(port.despawnEvents().getFirst().zoneId()).isEqualTo("zone-a");
    }

    @Test
    void recordsZoneChangeEvent() {
        port.onZoneChange("e2", "zone-b", 10.0, 20.0);

        assertThat(port.zoneChangeEvents()).hasSize(1);
        FakeGameEventPort.ZoneChangeEvent event = port.zoneChangeEvents().getFirst();
        assertThat(event.entityId()).isEqualTo("e2");
        assertThat(event.newZoneId()).isEqualTo("zone-b");
        assertThat(event.x()).isEqualTo(10.0);
        assertThat(event.y()).isEqualTo(20.0);
    }

    @Test
    void totalEventCountAggregatesAllTypes() {
        port.onEntityDespawn("e1", "z1");
        port.onEntityDespawn("e2", "z2");
        port.onZoneChange("e3", "z3", 0, 0);

        assertThat(port.totalEventCount()).isEqualTo(3);
    }

    @Test
    void clearResetsAllEvents() {
        port.onEntityDespawn("e1", "z1");
        port.onZoneChange("e2", "z2", 1, 1);

        port.clear();

        assertThat(port.totalEventCount()).isZero();
        assertThat(port.despawnEvents()).isEmpty();
        assertThat(port.zoneChangeEvents()).isEmpty();
    }

    @Test
    void multipleEventsAreRecordedInOrder() {
        port.onEntityDespawn("a", "z1");
        port.onEntityDespawn("b", "z2");
        port.onEntityDespawn("c", "z3");

        assertThat(port.despawnEvents())
                .extracting(FakeGameEventPort.DespawnEvent::entityId)
                .containsExactly("a", "b", "c");
    }
}
