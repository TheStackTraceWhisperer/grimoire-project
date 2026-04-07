package com.grimoire.application.core.system;

import com.grimoire.application.core.ecs.ComponentManager;
import com.grimoire.application.core.ecs.EcsWorld;
import com.grimoire.application.core.ecs.GameSystem;
import com.grimoire.application.core.port.GameConfig;
import com.grimoire.application.core.port.GameEventPort;
import com.grimoire.domain.core.component.*;

import java.util.Objects;

import static com.grimoire.application.core.ecs.ComponentManager.*;

/**
 * Handles zone transitions through portal collisions.
 *
 * <p>
 * Iterates the dense active-entity array using bitwise signature checks.
 * </p>
 */
public class ZoneChangeSystem implements GameSystem {

    /**
     * Player must have all of these to be a candidate for zone change.
     */
    private static final long PLAYER_MASK = BIT_PLAYER_CONTROLLED | BIT_ZONE
            | BIT_POSITION | BIT_BOUNDING_BOX;
    /**
     * Skip players that are on portal cooldown.
     */
    private static final long PLAYER_EXCLUDED = BIT_PORTAL_COOLDOWN;

    /**
     * Portal entities must have all of these.
     */
    private static final long PORTAL_MASK = BIT_PORTAL | BIT_ZONE | BIT_POSITION
            | BIT_BOUNDING_BOX;

    /**
     * Portal-find lookup mask.
     */
    private static final long PORTAL_FIND_MASK = BIT_PORTAL | BIT_ZONE | BIT_RENDERABLE
            | BIT_POSITION;

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
    public void tick(long currentTick) {
        int[] active = ecsWorld.getActiveEntities();
        int count = ecsWorld.getActiveCount();
        ComponentManager cm = ecsWorld.getComponentManager();
        long[] sigs = cm.getSignatures();
        Position[] positions = cm.getPositions();
        BoundingBox[] boxes = cm.getBoundingBoxes();
        Zone[] zones = cm.getZones();

        for (int j = 0; j < count; j++) {
            int playerId = active[j];
            if ((sigs[playerId] & PLAYER_MASK) != PLAYER_MASK) {
                continue;
            }
            if ((sigs[playerId] & PLAYER_EXCLUDED) != 0) {
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
        int[] active = ecsWorld.getActiveEntities();
        int count = ecsWorld.getActiveCount();
        long[] sigs = cm.getSignatures();
        Portal[] portals = cm.getPortals();
        Zone[] zones = cm.getZones();
        Position[] positions = cm.getPositions();
        BoundingBox[] boxes = cm.getBoundingBoxes();

        for (int j = 0; j < count; j++) {
            int portalId = active[j];
            if ((sigs[portalId] & PORTAL_MASK) != PORTAL_MASK) {
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

                cm.addZone(playerId, portal.targetZoneId);
                cm.addPosition(playerId, targetPos.x, targetPos.y);
                cm.addPortalCooldown(playerId, portalCooldownTicks);
                cm.addDirty(playerId, ecsWorld.getCurrentTick());

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
        int[] active = ecsWorld.getActiveEntities();
        int count = ecsWorld.getActiveCount();
        long[] sigs = cm.getSignatures();
        Zone[] zones = cm.getZones();
        Renderable[] renderables = cm.getRenderables();
        Position[] positions = cm.getPositions();

        for (int j = 0; j < count; j++) {
            int i = active[j];
            if ((sigs[i] & PORTAL_FIND_MASK) != PORTAL_FIND_MASK) {
                continue;
            }
            if (zones[i].zoneId.equals(zoneId)
                    && renderables[i].name.equals(portalName)) {
                return positions[i];
            }
        }
        return null;
    }
}
