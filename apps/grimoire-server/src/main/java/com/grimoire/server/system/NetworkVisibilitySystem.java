package com.grimoire.server.system;
import io.micronaut.core.annotation.Order;

import com.grimoire.server.component.*;
import com.grimoire.ecs.EcsWorld;
import com.grimoire.ecs.GameSystem;
import com.grimoire.shared.component.*;
import com.grimoire.shared.dto.EntitySpawn;
import com.grimoire.shared.protocol.GamePacket;
import com.grimoire.shared.protocol.PacketType;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles entity visibility when players change zones.
 */
@Order(1000)
@Singleton
@RequiredArgsConstructor
public class NetworkVisibilitySystem implements GameSystem {
    
    private final EcsWorld ecsWorld;
    // Track player zones from previous tick
    private final Map<String, String> previousPlayerZones = new ConcurrentHashMap<>();
    
    @Override
    public void tick(float deltaTime) {
        // Check for zone changes
        for (String playerId : ecsWorld.getEntitiesWithComponent(PlayerConnection.class)) {
            var currentZoneOpt = ecsWorld.getComponent(playerId, Zone.class);
            var playerConnOpt = ecsWorld.getComponent(playerId, PlayerConnection.class);
            
            if (currentZoneOpt.isEmpty() || playerConnOpt.isEmpty()) {
                continue;
            }
            
            String currentZone = currentZoneOpt.get().zoneId();
            String previousZone = previousPlayerZones.get(playerId);
            
            // Check if zone changed
            if (previousZone == null || !previousZone.equals(currentZone)) {
                // Player changed zones, send all entities in new zone
                sendZoneEntities(playerId, currentZone, playerConnOpt.get().channel());
                previousPlayerZones.put(playerId, currentZone);
            }
        }
        
        // Clean up disconnected players
        previousPlayerZones.keySet().removeIf(playerId -> 
                !ecsWorld.hasComponent(playerId, PlayerConnection.class));
    }
    
    private void sendZoneEntities(String playerId, String zoneId, io.netty.channel.Channel channel) {
        // Send EntitySpawn for all entities in the zone
        for (String entityId : ecsWorld.getAllEntities()) {
            // Skip the player itself
            if (entityId.equals(playerId)) {
                continue;
            }
            
            var entityZoneOpt = ecsWorld.getComponent(entityId, Zone.class);
            if (entityZoneOpt.isEmpty() || !entityZoneOpt.get().zoneId().equals(zoneId)) {
                continue;
            }
            
            List<ComponentDTO> allComponents = convertToComponentDTOs(entityId);
            if (!allComponents.isEmpty()) {
                EntitySpawn spawn = new EntitySpawn(entityId, allComponents);
                GamePacket packet = new GamePacket(PacketType.S2C_ENTITY_SPAWN, spawn);
                channel.writeAndFlush(packet);
            }
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
