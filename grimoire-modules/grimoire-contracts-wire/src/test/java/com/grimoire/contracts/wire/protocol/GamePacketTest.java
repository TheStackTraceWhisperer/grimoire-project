package com.grimoire.contracts.wire.protocol;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GamePacketTest {

    @Test
    void creationPreservesFields() {
        var packet = new GamePacket(PacketType.C2S_TOKEN_LOGIN_REQUEST, "token-123");

        assertThat(packet.type()).isEqualTo(PacketType.C2S_TOKEN_LOGIN_REQUEST);
        assertThat(packet.payload()).isEqualTo("token-123");
    }

    @Test
    void implementsSerializable() {
        var packet = new GamePacket(PacketType.S2C_CHARACTER_LIST, "data");

        assertThat(packet).isInstanceOf(java.io.Serializable.class);
    }

    @Test
    void nullPayloadIsAllowed() {
        var packet = new GamePacket(PacketType.S2C_ENTITY_DESPAWN, null);

        assertThat(packet.payload()).isNull();
    }

    @Test
    void equalityIsStructural() {
        var a = new GamePacket(PacketType.C2S_CHAT_MESSAGE, "hello");
        var b = new GamePacket(PacketType.C2S_CHAT_MESSAGE, "hello");

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }
}
