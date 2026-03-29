package com.grimoire.contracts.wire.protocol;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PacketTypeTest {

    @Test
    void allClientToServerTypesStartWithC2S() {
        var c2sTypes = java.util.Arrays.stream(PacketType.values())
                .filter(t -> t.name().startsWith("C2S_"))
                .toList();

        assertThat(c2sTypes).hasSize(10);
    }

    @Test
    void allServerToClientTypesStartWithS2C() {
        var s2cTypes = java.util.Arrays.stream(PacketType.values())
                .filter(t -> t.name().startsWith("S2C_"))
                .toList();

        assertThat(s2cTypes).hasSize(13);
    }

    @Test
    void totalPacketTypeCount() {
        assertThat(PacketType.values()).hasSize(23);
    }

    @Test
    void valueOfRoundTrips() {
        for (PacketType type : PacketType.values()) {
            assertThat(PacketType.valueOf(type.name())).isEqualTo(type);
        }
    }
}
