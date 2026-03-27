package com.grimoire.client.network;

import com.grimoire.client.JavaFxApplication;
import com.grimoire.client.state.ClientEcsWorld;
import com.grimoire.shared.dto.*;
import com.grimoire.shared.protocol.GamePacket;
import io.micronaut.context.annotation.Prototype;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import jakarta.inject.Inject;
import javafx.application.Platform;
import lombok.extern.slf4j.Slf4j;

/**
 * Client-side packet handler.
 */
@Prototype
@Slf4j
public class ClientLogicHandler extends SimpleChannelInboundHandler<GamePacket> {
    
    private final ClientEcsWorld clientEcsWorld;
    private final JavaFxApplication application;
    
    @Inject
    public ClientLogicHandler(ClientEcsWorld clientEcsWorld, JavaFxApplication application) {
        this.clientEcsWorld = clientEcsWorld;
        this.application = application;
    }
    
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GamePacket packet) {
        log.debug("Received packet: {}", packet.type());
        
        switch (packet.type()) {
            case S2C_CHARACTER_LIST:
                handleCharacterList(packet);
                break;
                
            case S2C_CHARACTER_SELECTION_SUCCESS:
                handleCharacterSelectionSuccess(packet);
                break;
                
            case S2C_LOGIN_FAILURE:
                handleLoginFailure(packet);
                break;
                
            case S2C_ZONE_CHANGE:
                handleZoneChange(packet);
                break;
                
            case S2C_GAME_STATE_UPDATE:
                handleGameStateUpdate(packet);
                break;
                
            case S2C_ENTITY_SPAWN:
                handleEntitySpawn(packet);
                break;
                
            case S2C_ENTITY_DESPAWN:
                handleEntityDespawn(packet);
                break;
                
            case S2C_CHAT_BROADCAST:
                handleChatBroadcast(packet);
                break;
                
            case S2C_PRIVATE_MESSAGE_BROADCAST:
                handlePrivateMessageBroadcast(packet);
                break;
                
            case S2C_CREATE_GROUP_RESPONSE:
                handleCreateGroupResponse(packet);
                break;
                
            case S2C_GROUP_MESSAGE_BROADCAST:
                handleGroupMessageBroadcast(packet);
                break;
                
            case S2C_JOIN_GROUP_RESPONSE:
                handleJoinGroupResponse(packet);
                break;
                
            case S2C_LEAVE_GROUP_RESPONSE:
                handleLeaveGroupResponse(packet);
                break;
                
            default:
                log.warn("Unknown packet type: {}", packet.type());
        }
    }
    
    private void handleCharacterList(GamePacket packet) {
        if (packet.payload() instanceof CharacterListResponse response) {
            log.info("Received character list, {} characters available", response.characters().size());
            Platform.runLater(() -> {
                try {
                    application.showCharacterSelectionScene(response);
                } catch (Exception e) {
                    log.error("Failed to show character selection scene", e);
                }
            });
        }
    }
    
    private void handleCharacterSelectionSuccess(GamePacket packet) {
        if (packet.payload() instanceof CharacterSelectionSuccess success) {
            log.info("Character selected: {}, entity: {}", success.characterName(), success.entityId());
            Platform.runLater(() -> {
                clientEcsWorld.setLocalPlayerEntityId(success.entityId());
                clientEcsWorld.setCurrentZone(success.zone());
                try {
                    application.showGameScene();
                } catch (Exception e) {
                    log.error("Failed to show game scene", e);
                }
            });
        }
    }
    
    private void handleLoginFailure(GamePacket packet) {
        if (packet.payload() instanceof LoginFailure failure) {
            log.warn("Login failed: {}", failure.reason());
            Platform.runLater(() -> {
                // Could show error dialog here
                log.error("Login failed: {}", failure.reason());
            });
        }
    }
    
    private void handleZoneChange(GamePacket packet) {
        if (packet.payload() instanceof ZoneChange zoneChange) {
            log.info("Zone change to: {}", zoneChange.newZoneId());
            Platform.runLater(() -> {
                clientEcsWorld.clearAllEntities();
                clientEcsWorld.setCurrentZone(zoneChange.newZoneId());
            });
        }
    }
    
    private void handleGameStateUpdate(GamePacket packet) {
        if (packet.payload() instanceof GameStateUpdate update) {
            Platform.runLater(() -> clientEcsWorld.processStateUpdate(update));
        }
    }
    
    private void handleEntitySpawn(GamePacket packet) {
        if (packet.payload() instanceof EntitySpawn spawn) {
            Platform.runLater(() -> clientEcsWorld.spawnEntity(spawn));
        }
    }
    
    private void handleEntityDespawn(GamePacket packet) {
        if (packet.payload() instanceof EntityDespawn despawn) {
            Platform.runLater(() -> clientEcsWorld.despawnEntity(despawn));
        }
    }
    
    private void handleChatBroadcast(GamePacket packet) {
        if (packet.payload() instanceof ChatBroadcast chat) {
            log.info("Chat: {}: {}", chat.sender(), chat.message());
        }
    }
    
    private void handlePrivateMessageBroadcast(GamePacket packet) {
        if (packet.payload() instanceof PrivateMessageBroadcast pm) {
            log.info("Private message from {}: {}", pm.sender(), pm.message());
        }
    }
    
    private void handleCreateGroupResponse(GamePacket packet) {
        if (packet.payload() instanceof CreateGroupResponse response) {
            if (response.success()) {
                log.info("Group created successfully: {}", response.message());
            } else {
                log.warn("Failed to create group: {}", response.message());
            }
        }
    }
    
    private void handleGroupMessageBroadcast(GamePacket packet) {
        if (packet.payload() instanceof GroupMessageBroadcast gm) {
            log.info("[{}] {}: {}", gm.groupName(), gm.sender(), gm.message());
        }
    }
    
    private void handleJoinGroupResponse(GamePacket packet) {
        if (packet.payload() instanceof JoinGroupResponse response) {
            if (response.success()) {
                log.info("Joined group: {}", response.message());
            } else {
                log.warn("Failed to join group: {}", response.message());
            }
        }
    }
    
    private void handleLeaveGroupResponse(GamePacket packet) {
        if (packet.payload() instanceof LeaveGroupResponse response) {
            if (response.success()) {
                log.info("Left group: {}", response.message());
            } else {
                log.warn("Failed to leave group: {}", response.message());
            }
        }
    }
    
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.info("Disconnected from server");
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Network error", cause);
        ctx.close();
    }
}
