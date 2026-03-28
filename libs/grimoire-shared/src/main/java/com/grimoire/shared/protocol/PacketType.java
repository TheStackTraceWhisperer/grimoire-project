package com.grimoire.shared.protocol;

/**
 * Network packet type enumeration for client-server communication.
 */
public enum PacketType {
    // Client-to-Server
    C2S_TOKEN_LOGIN_REQUEST,
    C2S_REQUEST_CHARACTER_LIST,
    C2S_CHARACTER_SELECTION,
    C2S_MOVEMENT_INTENT,
    C2S_CHAT_MESSAGE,
    C2S_PRIVATE_MESSAGE,
    C2S_CREATE_GROUP,
    C2S_GROUP_MESSAGE,
    C2S_JOIN_GROUP,
    C2S_LEAVE_GROUP,

    // Server-to-Client
    S2C_CHARACTER_LIST,
    S2C_CHARACTER_SELECTION_SUCCESS,
    S2C_LOGIN_FAILURE,
    S2C_GAME_STATE_UPDATE,  // Delta-update of component changes
    S2C_ENTITY_SPAWN,       // Full data for a new entity
    S2C_ENTITY_DESPAWN,     // An entity is removed
    S2C_ZONE_CHANGE,        // Client must clear its state
    S2C_CHAT_BROADCAST,
    S2C_PRIVATE_MESSAGE_BROADCAST,
    S2C_CREATE_GROUP_RESPONSE,
    S2C_GROUP_MESSAGE_BROADCAST,
    S2C_JOIN_GROUP_RESPONSE,
    S2C_LEAVE_GROUP_RESPONSE
}
