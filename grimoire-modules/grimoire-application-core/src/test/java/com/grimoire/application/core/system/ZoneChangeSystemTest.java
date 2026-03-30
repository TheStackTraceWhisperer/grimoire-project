package com.grimoire.application.core.system;

import com.grimoire.application.core.ecs.ComponentManager;
import com.grimoire.application.core.ecs.EcsWorld;
import com.grimoire.application.core.ecs.EntityManager;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class ZoneChangeSystemTest {

    private EcsWorld world;
    private GameEventPort gameEventPort;
    private ZoneChangeSystem system;

    @BeforeEach
    void setUp() {
        world = new EcsWorld(new EntityManager(), new ComponentManager());
        gameEventPort = Mockito.mock(GameEventPort.class);
        GameConfig config = new GameConfig() {
        };
        system = new ZoneChangeSystem(world, config, gameEventPort);
    }

    @Test
    void playerTeleportsOnPortalCollision() {
        String player = createPlayer(10, 10, "zone1");
        createPortalPair("zone1", "zone2");

        system.tick(0.05f);

        Zone zone = world.getComponent(player, Zone.class).orElseThrow();
        assertThat(zone.zoneId()).isEqualTo("zone2");
    }

    @Test
    void playerPositionUpdatedToTargetPortal() {
        String player = createPlayer(10, 10, "zone1");
        createPortalPair("zone1", "zone2");

        system.tick(0.05f);

        Position pos = world.getComponent(player, Position.class).orElseThrow();
        assertThat(pos.x()).isCloseTo(200.0, within(0.01));
        assertThat(pos.y()).isCloseTo(200.0, within(0.01));
    }

    @Test
    void portalCooldownApplied() {
        String player = createPlayer(10, 10, "zone1");
        createPortalPair("zone1", "zone2");

        system.tick(0.05f);

        assertThat(world.hasComponent(player, PortalCooldown.class)).isTrue();
    }

    @Test
    void gameEventPortNotified() {
        String player = createPlayer(10, 10, "zone1");
        createPortalPair("zone1", "zone2");

        system.tick(0.05f);

        verify(gameEventPort).onZoneChange(eq(player), eq("zone2"),
                eq(200.0), eq(200.0));
    }

    @Test
    void playerMarkedDirty() {
        String player = createPlayer(10, 10, "zone1");
        createPortalPair("zone1", "zone2");

        system.tick(0.05f);

        assertThat(world.hasComponent(player, Dirty.class)).isTrue();
    }

    @Test
    void playerWithCooldownDoesNotTeleport() {
        String player = createPlayer(10, 10, "zone1");
        world.addComponent(player, new PortalCooldown(30));
        createPortalPair("zone1", "zone2");

        system.tick(0.05f);

        Zone zone = world.getComponent(player, Zone.class).orElseThrow();
        assertThat(zone.zoneId()).isEqualTo("zone1");
    }

    @Test
    void noTeleportWhenPortalInDifferentZone() {
        String player = createPlayer(10, 10, "zone1");
        // Portal is in zone3, not zone1
        String portal = world.createEntity();
        world.addComponent(portal, new Portal("zone2", "target-portal"));
        world.addComponent(portal, new Position(10, 10));
        world.addComponent(portal, new BoundingBox(20, 20));
        world.addComponent(portal, new Zone("zone3"));

        system.tick(0.05f);

        verify(gameEventPort, never()).onZoneChange(anyString(), anyString(),
                anyDouble(), anyDouble());
    }

    @Test
    void noTeleportWhenNoCollision() {
        String player = createPlayer(1000, 1000, "zone1");
        createPortalPair("zone1", "zone2");

        system.tick(0.05f);

        Zone zone = world.getComponent(player, Zone.class).orElseThrow();
        assertThat(zone.zoneId()).isEqualTo("zone1");
    }

    @Test
    void noTeleportWhenTargetPortalNotFound() {
        String player = createPlayer(10, 10, "zone1");
        // Source portal exists but target portal doesn't
        String portal = world.createEntity();
        world.addComponent(portal, new Portal("zone2", "missing-portal"));
        world.addComponent(portal, new Position(10, 10));
        world.addComponent(portal, new BoundingBox(20, 20));
        world.addComponent(portal, new Zone("zone1"));

        system.tick(0.05f);

        Zone zone = world.getComponent(player, Zone.class).orElseThrow();
        assertThat(zone.zoneId()).isEqualTo("zone1");
    }

    private String createPlayer(double x, double y, String zoneId) {
        String player = world.createEntity();
        world.addComponent(player, new PlayerControlled("session-1"));
        world.addComponent(player, new Position(x, y));
        world.addComponent(player, new BoundingBox(4, 4));
        world.addComponent(player, new Zone(zoneId));
        return player;
    }

    /**
     * Creates a portal at (10,10) in sourceZone pointing to targetZone, and a
     * target portal at (200,200) in targetZone.
     */
    private void createPortalPair(String sourceZone, String targetZone) {
        // Source portal
        String sourcePortal = world.createEntity();
        world.addComponent(sourcePortal, new Portal(targetZone, "target-portal"));
        world.addComponent(sourcePortal, new Position(10, 10));
        world.addComponent(sourcePortal, new BoundingBox(20, 20));
        world.addComponent(sourcePortal, new Zone(sourceZone));

        // Target portal (looked up by Renderable.name matching targetPortalId)
        String targetPortal = world.createEntity();
        world.addComponent(targetPortal, new Portal(sourceZone, "source-portal"));
        world.addComponent(targetPortal, new Position(200, 200));
        world.addComponent(targetPortal, new BoundingBox(20, 20));
        world.addComponent(targetPortal, new Zone(targetZone));
        world.addComponent(targetPortal, new Renderable("target-portal", "portal-sprite"));
    }
}
