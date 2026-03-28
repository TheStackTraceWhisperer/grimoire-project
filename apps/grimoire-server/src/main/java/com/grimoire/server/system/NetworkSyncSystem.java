package com.grimoire.server.system;
import io.micronaut.core.annotation.Order;

import com.grimoire.server.component.*;
import com.grimoire.ecs.EcsWorld;
import com.grimoire.ecs.GameSystem;
import com.grimoire.shared.component.*;
import com.grimoire.shared.dto.GameStateUpdate;
import com.grimoire.shared.protocol.GamePacket;
import com.grimoire.shared.protocol.PacketType;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;

import java.util.*;

/**
 * Synchronizes game state with connected clients.
 */
@Order(900)
@Singleton
@RequiredArgsConstructor
public class NetworkSyncSystem implements GameSystem {
    
    private final EcsWorld ecsWorld;
    
    @Override
    public void tick(float deltaTime) {
        // For each online player, build a custom state update
        for (String playerId : ecsWorld.getEntitiesWithComponent(PlayerConnection.class)) {
            var playerZoneOpt = ecsWorld.getComponent(playerId, Zone.class);
            var playerConnOpt = ecsWorld.getComponent(playerId, PlayerConnection.class);
            
            if (playerZoneOpt.isEmpty() || playerConnOpt.isEmpty()) {
                continue;
            }
            
            Zone playerZone = playerZoneOpt.get();
            PlayerConnection playerConn = playerConnOpt.get();
            
            // Build entity updates for this player's zone
            Map<String, List<ComponentDTO>> entityUpdates = new HashMap<>();
            
            for (String entityId : ecsWorld.getEntitiesWithComponent(Dirty.class)) {
                // Only include entities in the same zone
                var entityZoneOpt = ecsWorld.getComponent(entityId, Zone.class);
                if (entityZoneOpt.isEmpty() || !entityZoneOpt.get().zoneId().equals(playerZone.zoneId())) {
                    continue;
                }
                
                List<ComponentDTO> dtos = convertToComponentDTOs(entityId);
                if (!dtos.isEmpty()) {
                    entityUpdates.put(entityId, dtos);
                }
            }
            
            // Send update if there are changes
            if (!entityUpdates.isEmpty()) {
                GameStateUpdate update = new GameStateUpdate(ecsWorld.getCurrentTick(), entityUpdates);
                GamePacket packet = new GamePacket(PacketType.S2C_GAME_STATE_UPDATE, update);
                playerConn.channel().writeAndFlush(packet);
            }
        }
        
        // Remove all dirty components after processing
        for (String entityId : ecsWorld.getEntitiesWithComponent(Dirty.class)) {
            ecsWorld.removeComponent(entityId, Dirty.class);
        }
    }
    
    private List<ComponentDTO> convertToComponentDTOs(String entityId) {
        List<ComponentDTO> dtos = new ArrayList<>();
        
        // Convert server components to DTOs
        ecsWorld.getComponent(entityId, Position.class).ifPresent(pos ->
                dtos.add(new PositionDTO(pos.x(), pos.y())));
        
        ecsWorld.getComponent(entityId, Renderable.class).ifPresent(render ->
                dtos.add(new RenderableDTO(render.name(), render.visualId())));
        
        ecsWorld.getComponent(entityId, Stats.class).ifPresent(stats ->
                dtos.add(new StatsDTO(stats.hp(), stats.maxHp())));
        
        ecsWorld.getComponent(entityId, BoundingBox.class).ifPresent(bbox -> {
            if (ecsWorld.hasComponent(entityId, Portal.class)) {
                dtos.add(new PortalDTO(bbox.width(), bbox.height()));
            }
        });
        
        return dtos;
    }
}
