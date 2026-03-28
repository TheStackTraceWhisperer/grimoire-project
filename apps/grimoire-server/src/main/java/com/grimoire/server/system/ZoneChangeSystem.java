package com.grimoire.server.system;
import io.micronaut.core.annotation.Order;

import com.grimoire.server.component.*;
import com.grimoire.ecs.EcsWorld;
import com.grimoire.ecs.GameSystem;
import com.grimoire.shared.dto.ZoneChange;
import com.grimoire.shared.protocol.GamePacket;
import com.grimoire.shared.protocol.PacketType;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;

/**
 * Handles zone transitions through portals.
 */
@Order(800)
@Singleton
@RequiredArgsConstructor
public class ZoneChangeSystem implements GameSystem {
    
    private final EcsWorld ecsWorld;
    
    @Override
    public void tick(float deltaTime) {
        // Process players who might be colliding with portals
        for (String playerId : ecsWorld.getEntitiesWithComponent(PlayerConnection.class)) {
            // Skip if player has portal cooldown
            if (ecsWorld.hasComponent(playerId, PortalCooldown.class)) {
                continue;
            }
            
            var playerZoneOpt = ecsWorld.getComponent(playerId, Zone.class);
            var playerPosOpt = ecsWorld.getComponent(playerId, Position.class);
            var playerBBoxOpt = ecsWorld.getComponent(playerId, BoundingBox.class);
            var playerConnOpt = ecsWorld.getComponent(playerId, PlayerConnection.class);
            
            if (playerZoneOpt.isEmpty() || playerPosOpt.isEmpty() || 
                    playerBBoxOpt.isEmpty() || playerConnOpt.isEmpty()) {
                continue;
            }
            
            Zone playerZone = playerZoneOpt.get();
            Position playerPos = playerPosOpt.get();
            BoundingBox playerBBox = playerBBoxOpt.get();
            PlayerConnection playerConn = playerConnOpt.get();
            
            // Check collision with portals in the same zone
            for (String portalId : ecsWorld.getEntitiesWithComponent(Portal.class)) {
                var portalZoneOpt = ecsWorld.getComponent(portalId, Zone.class);
                var portalPosOpt = ecsWorld.getComponent(portalId, Position.class);
                var portalBBoxOpt = ecsWorld.getComponent(portalId, BoundingBox.class);
                var portalDataOpt = ecsWorld.getComponent(portalId, Portal.class);
                
                if (portalZoneOpt.isEmpty() || portalPosOpt.isEmpty() || 
                        portalBBoxOpt.isEmpty() || portalDataOpt.isEmpty()) {
                    continue;
                }
                
                // Check if portal is in same zone
                if (!portalZoneOpt.get().zoneId().equals(playerZone.zoneId())) {
                    continue;
                }
                
                Position portalPos = portalPosOpt.get();
                BoundingBox portalBBox = portalBBoxOpt.get();
                Portal portalData = portalDataOpt.get();
                
                // Check AABB collision
                if (checkCollision(playerPos, playerBBox, portalPos, portalBBox)) {
                    // Find target portal position
                    String targetZoneId = portalData.targetZoneId();
                    String targetPortalId = portalData.targetPortalId();
                    
                    // Find the target portal
                    Position targetPos = findPortalPosition(targetZoneId, targetPortalId);
                    if (targetPos == null) {
                        continue; // Target portal not found
                    }
                    
                    // Send zone change packet
                    ZoneChange zoneChange = new ZoneChange(targetZoneId, targetPos.x(), targetPos.y());
                    GamePacket packet = new GamePacket(PacketType.S2C_ZONE_CHANGE, zoneChange);
                    playerConn.channel().writeAndFlush(packet);
                    
                    // Update player's zone and position
                    ecsWorld.addComponent(playerId, new Zone(targetZoneId));
                    ecsWorld.addComponent(playerId, targetPos);
                    
                    // Add portal cooldown (3 seconds = 60 ticks at 20 TPS)
                    ecsWorld.addComponent(playerId, new PortalCooldown(60));
                    
                    break; // Only process one portal collision per tick
                }
            }
        }
    }
    
    private boolean checkCollision(Position pos1, BoundingBox bbox1, Position pos2, BoundingBox bbox2) {
        return pos1.x() < pos2.x() + bbox2.width() &&
               pos1.x() + bbox1.width() > pos2.x() &&
               pos1.y() < pos2.y() + bbox2.height() &&
               pos1.y() + bbox1.height() > pos2.y();
    }
    
    private Position findPortalPosition(String zoneId, String portalId) {
        for (String entityId : ecsWorld.getEntitiesWithComponent(Portal.class)) {
            var zoneOpt = ecsWorld.getComponent(entityId, Zone.class);
            var renderableOpt = ecsWorld.getComponent(entityId, Renderable.class);
            var posOpt = ecsWorld.getComponent(entityId, Position.class);
            
            if (zoneOpt.isPresent() && renderableOpt.isPresent() && posOpt.isPresent() &&
                    zoneOpt.get().zoneId().equals(zoneId) &&
                    renderableOpt.get().name().equals(portalId)) {
                return posOpt.get();
            }
        }
        return null;
    }
}
