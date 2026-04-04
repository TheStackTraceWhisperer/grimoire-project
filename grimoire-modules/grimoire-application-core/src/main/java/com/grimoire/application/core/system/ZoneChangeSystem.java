package com.grimoire.application.core.system;

import com.grimoire.application.core.ecs.ComponentManager;
import com.grimoire.application.core.ecs.EcsWorld;
import com.grimoire.application.core.ecs.GameSystem;
import com.grimoire.application.core.port.GameConfig;
import com.grimoire.application.core.port.GameEventPort;
import com.grimoire.domain.core.component.BoundingBox;
import com.grimoire.domain.core.component.Dirty;
import com.grimoire.domain.core.component.PlayerControlled;
import com.grimoire.domain.core.component.Portal;
import com.grimoire.domain.core.component.PortalCooldown;
import com.grimoire.domain.core.component.Position;
import com.grimoire.domain.core.component.Renderable;
import com.grimoire.domain.core.component.Zone;

import java.util.Objects;

/**
 * Handles zone transitions through portal collisions.
 *
 * <p>
 * Iterates entities using contiguous for-loops over component arrays.
 * </p>
 */
public class ZoneChangeSystem implements GameSystem {

    private final EcsWorld ecsWorld;

    private final GameEventPort gameEventPort;
    private final int portalCooldownTicks;

    /**
     * Creates a zone change system.
     *
     * @param ecsWorld
     *            the ECS world
     * @param gameConfig
     *            configuration providing portal cooldown
     * @param gameEventPort
     *            port for zone-change notifications
     */
    public ZoneChangeSystem(EcsWorld ecsWorld, GameConfig gameConfig,
            GameEventPort gameEventPort) {
        this.ecsWorld = Objects.requireNonNull(ecsWorld, "ecsWorld must not be null");
        Objects.requireNonNull(gameConfig, "gameConfig must not be null");
        this.gameEventPort = Objects.requireNonNull(gameEventPort,
                "gameEventPort must not be null");
        this.portalCooldownTicks = gameConfig.portalCooldownTicks();
    }

    @Override
    public void tick(float deltaTime) {
        int max = ecsWorld.getMaxEntityId();
        boolean[] alive = ecsWorld.getAlive();
        ComponentManager cm = ecsWorld.getComponentManager();
        PlayerControlled[] pcs = cm.getPlayerControlled();
        PortalCooldown[] cooldowns = cm.getPortalCooldowns();
        Zone[] zones = cm.getZones();
        Position[] positions = cm.getPositions();
        BoundingBox[] boxes = cm.getBoundingBoxes();

        for (int playerId = 0; playerId < max; playerId++) {
            if (!alive[playerId] || pcs[playerId] == null) {
                continue;
            }
            if (cooldowns[playerId] != null) {
                continue;
            }
            if (zones[playerId] == null || positions[playerId] == null
                    || boxes[playerId] == null) {
                continue;
            }

            if (tryPortalTransition(playerId, zones[playerId], positions[playerId],
                    boxes[playerId], cm)) {
                break;
            }
        }
    }

    private boolean tryPortalTransition(int playerId, Zone playerZone,
            Position playerPos, BoundingBox playerBox, ComponentManager cm) {
        int max = ecsWorld.getMaxEntityId();
        boolean[] alive = ecsWorld.getAlive();
        Portal[] portals = cm.getPortals();
        Zone[] zones = cm.getZones();
        Position[] positions = cm.getPositions();
        BoundingBox[] boxes = cm.getBoundingBoxes();

        for (int portalId = 0; portalId < max; portalId++) {
            if (!alive[portalId] || portals[portalId] == null) {
                continue;
            }
            if (zones[portalId] == null || positions[portalId] == null
                    || boxes[portalId] == null) {
                continue;
            }
            if (!zones[portalId].zoneId.equals(playerZone.zoneId)) {
                continue;
            }

            if (checkAabbCollision(playerPos, playerBox,
                    positions[portalId], boxes[portalId])) {
                Portal portal = portals[portalId];
                Position targetPos = findPortalPosition(portal.targetZoneId,
                        portal.targetPortalId, cm);
                if (targetPos == null) {
                    continue;
                }

                zones[playerId] = new Zone(portal.targetZoneId);
                positions[playerId] = new Position(targetPos.x, targetPos.y);
                cm.getPortalCooldowns()[playerId] = new PortalCooldown(portalCooldownTicks);
                Dirty[] dirties = cm.getDirties();
                if (dirties[playerId] == null) {
                    dirties[playerId] = new Dirty(ecsWorld.getCurrentTick());
                } else {
                    dirties[playerId].tick = ecsWorld.getCurrentTick();
                }

                gameEventPort.onZoneChange(playerId, portal.targetZoneId,
                        targetPos.x, targetPos.y);
                return true;
            }
        }
        return false;
    }

    private boolean checkAabbCollision(Position pos1, BoundingBox box1,
            Position pos2, BoundingBox box2) {
        double left1 = pos1.x - box1.width / 2;
        double right1 = pos1.x + box1.width / 2;
        double top1 = pos1.y - box1.height / 2;
        double bottom1 = pos1.y + box1.height / 2;

        double left2 = pos2.x - box2.width / 2;
        double right2 = pos2.x + box2.width / 2;
        double top2 = pos2.y - box2.height / 2;
        double bottom2 = pos2.y + box2.height / 2;

        return left1 < right2 && right1 > left2
                && top1 < bottom2 && bottom1 > top2;
    }

    private Position findPortalPosition(String zoneId, String portalName,
            ComponentManager cm) {
        int max = ecsWorld.getMaxEntityId();
        boolean[] alive = ecsWorld.getAlive();
        Portal[] portals = cm.getPortals();
        Zone[] zones = cm.getZones();
        Renderable[] renderables = cm.getRenderables();
        Position[] positions = cm.getPositions();

        for (int i = 0; i < max; i++) {
            if (!alive[i] || portals[i] == null) {
                continue;
            }
            if (zones[i] != null && renderables[i] != null && positions[i] != null
                    && zones[i].zoneId.equals(zoneId)
                    && renderables[i].name.equals(portalName)) {
                return positions[i];
            }
        }
        return null;
    }
}
