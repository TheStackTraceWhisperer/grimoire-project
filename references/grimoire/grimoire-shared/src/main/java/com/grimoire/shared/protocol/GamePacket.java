package com.grimoire.shared.protocol;

import java.io.Serializable;

/**
 * Network packet wrapper for all client-server communication.
 */
public record GamePacket(PacketType type, Serializable payload) implements Serializable {
}
