package com.grimoire.application.core.system;

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
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.Objects;
import java.util.Optional;

/**
 * Handles zone transitions through portal collisions.
 *
 * <p>
 * Each tick, iterates entities with {@link PlayerControlled} and checks for
 * AABB overlap with {@link Portal} entities in the same zone. On collision the
 * entity is teleported to the target zone/position, a {@link PortalCooldown} is
 * applied, and the infrastructure layer is notified via
 * {@link GameEventPort#onZoneChange}.
 * </p>
 */
public class ZoneChangeSystem implements GameSystem {

    /** The ECS world. */
    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "EcsWorld is a managed collaborator, not external mutable data")
    private final EcsWorld ecsWorld;

    /** Port for zone-change notifications. */
    private final GameEventPort gameEventPort;

    /** Portal cooldown duration in ticks. */
    private final int portalCooldownTicks;

    /**
     * Creates a zone change system.
     *
     * @param ecsWorld
     *            the ECS world
     * @param gameConfig
     *            configuration providing portal cooldown duration
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
        for (String playerId : ecsWorld.getEntitiesWithComponent(PlayerControlled.class)) {
            if (ecsWorld.hasComponent(playerId, PortalCooldown.class)) {
                continue;
            }

            Optional<Zone> playerZoneOpt = ecsWorld.getComponent(playerId, Zone.class);
            Optional<Position> playerPosOpt = ecsWorld.getComponent(playerId, Position.class);
            Optional<BoundingBox> playerBoxOpt = ecsWorld.getComponent(playerId,
                    BoundingBox.class);

            if (playerZoneOpt.isEmpty() || playerPosOpt.isEmpty()
                    || playerBoxOpt.isEmpty()) {
                continue;
            }

            Zone playerZone = playerZoneOpt.get();
            Position playerPos = playerPosOpt.get();
            BoundingBox playerBox = playerBoxOpt.get();

            if (tryPortalTransition(playerId, playerZone, playerPos, playerBox)) {
                break;
            }
        }
    }

    /**
     * Attempts a portal transition for the given player, checking all portals in
     * the same zone.
     *
     * @param playerId
     *            the player entity ID
     * @param playerZone
     *            the player's current zone
     * @param playerPos
     *            the player's current position
     * @param playerBox
     *            the player's bounding box
     * @return {@code true} if a transition occurred
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private boolean tryPortalTransition(String playerId, Zone playerZone,
            Position playerPos, BoundingBox playerBox) {
        for (String portalId : ecsWorld.getEntitiesWithComponent(Portal.class)) {
            Optional<Zone> portalZoneOpt = ecsWorld.getComponent(portalId, Zone.class);
            Optional<Position> portalPosOpt = ecsWorld.getComponent(portalId, Position.class);
            Optional<BoundingBox> portalBoxOpt = ecsWorld.getComponent(portalId,
                    BoundingBox.class);
            Optional<Portal> portalOpt = ecsWorld.getComponent(portalId, Portal.class);

            if (portalZoneOpt.isEmpty() || portalPosOpt.isEmpty()
                    || portalBoxOpt.isEmpty() || portalOpt.isEmpty()) {
                continue;
            }

            if (!portalZoneOpt.get().zoneId().equals(playerZone.zoneId())) {
                continue;
            }

            if (checkAabbCollision(playerPos, playerBox,
                    portalPosOpt.get(), portalBoxOpt.get())) {
                Portal portal = portalOpt.get();
                Position targetPos = findPortalPosition(portal.targetZoneId(),
                        portal.targetPortalId());
                if (targetPos == null) {
                    continue;
                }

                ecsWorld.addComponent(playerId, new Zone(portal.targetZoneId()));
                ecsWorld.addComponent(playerId, targetPos);
                ecsWorld.addComponent(playerId, new PortalCooldown(portalCooldownTicks));
                ecsWorld.addComponent(playerId, new Dirty(ecsWorld.getCurrentTick()));

                gameEventPort.onZoneChange(playerId, portal.targetZoneId(),
                        targetPos.x(), targetPos.y());
                return true;
            }
        }
        return false;
    }

    /**
     * Performs an axis-aligned bounding-box overlap check between two entities.
     *
     * @param pos1
     *            position of the first entity
     * @param box1
     *            bounding box of the first entity
     * @param pos2
     *            position of the second entity
     * @param box2
     *            bounding box of the second entity
     * @return {@code true} if the bounding boxes overlap
     */
    private boolean checkAabbCollision(Position pos1, BoundingBox box1,
            Position pos2, BoundingBox box2) {
        double left1 = pos1.x() - box1.width() / 2;
        double right1 = pos1.x() + box1.width() / 2;
        double top1 = pos1.y() - box1.height() / 2;
        double bottom1 = pos1.y() + box1.height() / 2;

        double left2 = pos2.x() - box2.width() / 2;
        double right2 = pos2.x() + box2.width() / 2;
        double top2 = pos2.y() - box2.height() / 2;
        double bottom2 = pos2.y() + box2.height() / 2;

        return left1 < right2 && right1 > left2
                && top1 < bottom2 && bottom1 > top2;
    }

    /**
     * Locates a portal entity's position by zone and renderable name.
     *
     * @param zoneId
     *            the target zone ID
     * @param portalId
     *            the portal name (matched against {@link Renderable#name()})
     * @return the portal position, or {@code null} if not found
     */
    private Position findPortalPosition(String zoneId, String portalId) {
        for (String entityId : ecsWorld.getEntitiesWithComponent(Portal.class)) {
            Optional<Zone> zoneOpt = ecsWorld.getComponent(entityId, Zone.class);
            Optional<Renderable> renderOpt = ecsWorld.getComponent(entityId, Renderable.class);
            Optional<Position> posOpt = ecsWorld.getComponent(entityId, Position.class);

            if (zoneOpt.isPresent() && renderOpt.isPresent() && posOpt.isPresent()
                    && zoneOpt.get().zoneId().equals(zoneId)
                    && renderOpt.get().name().equals(portalId)) {
                return posOpt.get();
            }
        }
        return null;
    }
}
