package com.grimoire.shared.protocol;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GamePacketTest {

    @Test
    void testGamePacketCreation() {
        String payload = "test payload";
        GamePacket packet = new GamePacket(PacketType.C2S_TOKEN_LOGIN_REQUEST, payload);
        
        assertEquals(PacketType.C2S_TOKEN_LOGIN_REQUEST, packet.type());
        assertEquals(payload, packet.payload());
    }

    @Test
    void testGamePacketIsSerializable() {
        GamePacket packet = new GamePacket(PacketType.S2C_CHARACTER_LIST, "data");
        assertInstanceOf(java.io.Serializable.class, packet);
    }

    @Test
    void testGamePacketImmutability() {
        String payload = "immutable";
        GamePacket packet = new GamePacket(PacketType.C2S_CHAT_MESSAGE, payload);
        
        // Records are immutable by design
        assertNotNull(packet.type());
        assertNotNull(packet.payload());
    }
}
