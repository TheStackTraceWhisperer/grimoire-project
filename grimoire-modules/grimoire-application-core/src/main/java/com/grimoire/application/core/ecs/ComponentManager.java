package com.grimoire.application.core.ecs;

import com.grimoire.domain.combat.component.AttackCooldown;
import com.grimoire.domain.combat.component.AttackIntent;
import com.grimoire.domain.combat.component.Monster;
import com.grimoire.domain.combat.component.NpcAi;
import com.grimoire.domain.core.component.*;
import com.grimoire.domain.navigation.component.Path;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages component data using fixed-size contiguous arrays with bitwise
 * component signatures for O(1) archetype checks.
 *
 * <p>
 * Each component type has a dedicated array of size
 * {@link EntityManager#MAX_ENTITIES}. Entity IDs are used directly as array
 * indices. A {@code null} entry means the entity does not have that component.
 * </p>
 *
 * <p>
 * A {@code long[] signatures} array tracks which components each entity has.
 * Systems declare a {@code REQUIRED_MASK} and perform a single bitwise check
 * instead of multiple null-checks per entity.
 * </p>
 *
 * <p>
 * <strong>Thread-safety:</strong> This class must only be accessed from the
 * single-threaded game loop.
 * </p>
 */
public class ComponentManager {

    /**
     * Signature bit for {@link Position}.
     */
    public static final long BIT_POSITION = 1L;

    // ── Signature bit constants (one per component type) ──
    /**
     * Signature bit for {@link Velocity}.
     */
    public static final long BIT_VELOCITY = 1L << 1;
    /**
     * Signature bit for {@link Stats}.
     */
    public static final long BIT_STATS = 1L << 2;
    /**
     * Signature bit for {@link BoundingBox}.
     */
    public static final long BIT_BOUNDING_BOX = 1L << 3;
    /**
     * Signature bit for {@link Dead}.
     */
    public static final long BIT_DEAD = 1L << 4;
    /**
     * Signature bit for {@link Dirty}.
     */
    public static final long BIT_DIRTY = 1L << 5;
    /**
     * Signature bit for {@link Experience}.
     */
    public static final long BIT_EXPERIENCE = 1L << 6;
    /**
     * Signature bit for {@link MovementIntent}.
     */
    public static final long BIT_MOVEMENT_INTENT = 1L << 7;
    /**
     * Signature bit for {@link Persistent}.
     */
    public static final long BIT_PERSISTENT = 1L << 8;
    /**
     * Signature bit for {@link PlayerControlled}.
     */
    public static final long BIT_PLAYER_CONTROLLED = 1L << 9;
    /**
     * Signature bit for {@link Portal}.
     */
    public static final long BIT_PORTAL = 1L << 10;
    /**
     * Signature bit for {@link PortalCooldown}.
     */
    public static final long BIT_PORTAL_COOLDOWN = 1L << 11;
    /**
     * Signature bit for {@link Renderable}.
     */
    public static final long BIT_RENDERABLE = 1L << 12;
    /**
     * Signature bit for {@link Solid}.
     */
    public static final long BIT_SOLID = 1L << 13;
    /**
     * Signature bit for {@link SpawnPoint}.
     */
    public static final long BIT_SPAWN_POINT = 1L << 14;
    /**
     * Signature bit for {@link Zone}.
     */
    public static final long BIT_ZONE = 1L << 15;
    /**
     * Signature bit for {@link AttackCooldown}.
     */
    public static final long BIT_ATTACK_COOLDOWN = 1L << 16;
    /**
     * Signature bit for {@link AttackIntent}.
     */
    public static final long BIT_ATTACK_INTENT = 1L << 17;
    /**
     * Signature bit for {@link Monster}.
     */
    public static final long BIT_MONSTER = 1L << 18;
    /**
     * Signature bit for {@link NpcAi}.
     */
    public static final long BIT_NPC_AI = 1L << 19;
    /**
     * Signature bit for {@link Path}.
     */
    public static final long BIT_PATH = 1L << 20;
    private static final int MAX = EntityManager.MAX_ENTITIES;

    // ── Signature array ──
    private static final Map<Class<? extends Component>, Long> BIT_MAP = Map.ofEntries(
            Map.entry(Position.class, BIT_POSITION),
            Map.entry(Velocity.class, BIT_VELOCITY),
            Map.entry(Stats.class, BIT_STATS),
            Map.entry(BoundingBox.class, BIT_BOUNDING_BOX),
            Map.entry(Dead.class, BIT_DEAD),
            Map.entry(Dirty.class, BIT_DIRTY),
            Map.entry(Experience.class, BIT_EXPERIENCE),
            Map.entry(MovementIntent.class, BIT_MOVEMENT_INTENT),
            Map.entry(Persistent.class, BIT_PERSISTENT),
            Map.entry(PlayerControlled.class, BIT_PLAYER_CONTROLLED),
            Map.entry(Portal.class, BIT_PORTAL),
            Map.entry(PortalCooldown.class, BIT_PORTAL_COOLDOWN),
            Map.entry(Renderable.class, BIT_RENDERABLE),
            Map.entry(Solid.class, BIT_SOLID),
            Map.entry(SpawnPoint.class, BIT_SPAWN_POINT),
            Map.entry(Zone.class, BIT_ZONE),
            Map.entry(AttackCooldown.class, BIT_ATTACK_COOLDOWN),
            Map.entry(AttackIntent.class, BIT_ATTACK_INTENT),
            Map.entry(Monster.class, BIT_MONSTER),
            Map.entry(NpcAi.class, BIT_NPC_AI),
            Map.entry(Path.class, BIT_PATH));

    // ── Class-to-bit mapping for generic access ──
    /**
     * Bitwise component signature per entity for O(1) archetype checks.
     */
    private final long[] signatures = new long[MAX];
    // ── Domain-core component arrays ──
    private final Position[] positions = new Position[MAX];
    private final Velocity[] velocities = new Velocity[MAX];
    private final Stats[] stats = new Stats[MAX];
    private final BoundingBox[] boundingBoxes = new BoundingBox[MAX];
    private final Dead[] deads = new Dead[MAX];
    private final Dirty[] dirties = new Dirty[MAX];
    private final Experience[] experiences = new Experience[MAX];
    private final MovementIntent[] movementIntents = new MovementIntent[MAX];
    private final Persistent[] persistents = new Persistent[MAX];
    private final PlayerControlled[] playerControlled = new PlayerControlled[MAX];
    private final Portal[] portals = new Portal[MAX];
    private final PortalCooldown[] portalCooldowns = new PortalCooldown[MAX];
    private final Renderable[] renderables = new Renderable[MAX];
    private final Solid[] solids = new Solid[MAX];
    private final SpawnPoint[] spawnPoints = new SpawnPoint[MAX];
    private final Zone[] zones = new Zone[MAX];

    // ── Domain-combat component arrays ──
    private final AttackCooldown[] attackCooldowns = new AttackCooldown[MAX];
    private final AttackIntent[] attackIntents = new AttackIntent[MAX];
    private final Monster[] monsters = new Monster[MAX];
    private final NpcAi[] npcAis = new NpcAi[MAX];

    // ── Domain-navigation component arrays ──
    private final Path[] paths = new Path[MAX];

    // ── Type-to-array mapping for generic access ──
    private final Map<Class<? extends Component>, Component[]> arrayMap = new HashMap<>();

    /**
     * Initializes the component-type-to-array mapping.
     */
    public ComponentManager() {
        arrayMap.put(Position.class, positions);
        arrayMap.put(Velocity.class, velocities);
        arrayMap.put(Stats.class, stats);
        arrayMap.put(BoundingBox.class, boundingBoxes);
        arrayMap.put(Dead.class, deads);
        arrayMap.put(Dirty.class, dirties);
        arrayMap.put(Experience.class, experiences);
        arrayMap.put(MovementIntent.class, movementIntents);
        arrayMap.put(Persistent.class, persistents);
        arrayMap.put(PlayerControlled.class, playerControlled);
        arrayMap.put(Portal.class, portals);
        arrayMap.put(PortalCooldown.class, portalCooldowns);
        arrayMap.put(Renderable.class, renderables);
        arrayMap.put(Solid.class, solids);
        arrayMap.put(SpawnPoint.class, spawnPoints);
        arrayMap.put(Zone.class, zones);
        arrayMap.put(AttackCooldown.class, attackCooldowns);
        arrayMap.put(AttackIntent.class, attackIntents);
        arrayMap.put(Monster.class, monsters);
        arrayMap.put(NpcAi.class, npcAis);
        arrayMap.put(Path.class, paths);
    }

    // ── Signature accessors ──

    /**
     * Returns the signature array for direct system access.
     *
     * @return the signature array indexed by entity ID
     */
    public long[] getSignatures() {
        return signatures;
    }

    // ── Typed add methods (zero-allocation reuse, wipe-and-set, maintain sigs) ──

    /**
     * Adds or resets a {@link Position} component, wiping all fields.
     *
     * @param entityId
     *            the entity ID
     * @param x
     *            the X coordinate
     * @param y
     *            the Y coordinate
     */
    public void addPosition(int entityId, double x, double y) {
        Position p = positions[entityId];
        if (p == null) {
            p = new Position();
            positions[entityId] = p;
        }
        p.update(x, y);
        signatures[entityId] |= BIT_POSITION;
    }

    /**
     * Adds or resets a {@link Velocity} component, wiping all fields.
     *
     * @param entityId
     *            the entity ID
     * @param dx
     *            velocity along X
     * @param dy
     *            velocity along Y
     */
    public void addVelocity(int entityId, double dx, double dy) {
        Velocity v = velocities[entityId];
        if (v == null) {
            v = new Velocity();
            velocities[entityId] = v;
        }
        v.update(dx, dy);
        signatures[entityId] |= BIT_VELOCITY;
    }

    /**
     * Adds or resets a {@link Stats} component, wiping all fields.
     *
     * @param entityId
     *            the entity ID
     * @param hp
     *            current hit points
     * @param maxHp
     *            maximum hit points
     * @param defense
     *            defense rating
     * @param attack
     *            attack rating
     */
    public void addStats(int entityId, int hp, int maxHp, int defense, int attack) {
        Stats s = stats[entityId];
        if (s == null) {
            s = new Stats();
            stats[entityId] = s;
        }
        s.update(hp, maxHp, defense, attack);
        signatures[entityId] |= BIT_STATS;
    }

    /**
     * Adds or resets a {@link Dirty} component, wiping all fields.
     *
     * @param entityId
     *            the entity ID
     * @param tick
     *            the game tick
     */
    public void addDirty(int entityId, long tick) {
        Dirty d = dirties[entityId];
        if (d == null) {
            d = new Dirty();
            dirties[entityId] = d;
        }
        d.update(tick);
        signatures[entityId] |= BIT_DIRTY;
    }

    /**
     * Adds or resets a {@link Dead} component, wiping all fields.
     *
     * @param entityId
     *            the entity ID
     * @param killerId
     *            the killer's entity ID
     */
    public void addDead(int entityId, int killerId) {
        Dead d = deads[entityId];
        if (d == null) {
            d = new Dead();
            deads[entityId] = d;
        }
        d.update(killerId);
        signatures[entityId] |= BIT_DEAD;
    }

    /**
     * Adds or resets an {@link AttackCooldown} component, wiping all fields.
     *
     * @param entityId
     *            the entity ID
     * @param ticksRemaining
     *            ticks until next attack
     */
    public void addAttackCooldown(int entityId, int ticksRemaining) {
        AttackCooldown cd = attackCooldowns[entityId];
        if (cd == null) {
            cd = new AttackCooldown();
            attackCooldowns[entityId] = cd;
        }
        cd.update(ticksRemaining);
        signatures[entityId] |= BIT_ATTACK_COOLDOWN;
    }

    /**
     * Adds or resets an {@link AttackIntent} component, wiping all fields.
     *
     * @param entityId
     *            the entity ID
     * @param targetEntityId
     *            the target entity ID
     */
    public void addAttackIntent(int entityId, int targetEntityId) {
        AttackIntent ai = attackIntents[entityId];
        if (ai == null) {
            ai = new AttackIntent();
            attackIntents[entityId] = ai;
        }
        ai.update(targetEntityId);
        signatures[entityId] |= BIT_ATTACK_INTENT;
    }

    /**
     * Adds or resets a {@link Zone} component, wiping all fields.
     *
     * @param entityId
     *            the entity ID
     * @param zoneId
     *            the zone identifier
     */
    public void addZone(int entityId, String zoneId) {
        Zone z = zones[entityId];
        if (z == null) {
            z = new Zone();
            zones[entityId] = z;
        }
        z.update(zoneId);
        signatures[entityId] |= BIT_ZONE;
    }

    /**
     * Adds or resets a {@link PortalCooldown} component, wiping all fields.
     *
     * @param entityId
     *            the entity ID
     * @param ticksRemaining
     *            ticks until cooldown expires
     */
    public void addPortalCooldown(int entityId, long ticksRemaining) {
        PortalCooldown pc = portalCooldowns[entityId];
        if (pc == null) {
            pc = new PortalCooldown();
            portalCooldowns[entityId] = pc;
        }
        pc.update(ticksRemaining);
        signatures[entityId] |= BIT_PORTAL_COOLDOWN;
    }

    /**
     * Adds or resets a {@link Path} component, clearing the waypoints list and
     * resetting all fields.
     *
     * @param entityId
     *            the entity ID
     * @param waypoints
     *            the waypoints (may be {@code null} for an empty path)
     * @param targetEntityId
     *            the entity being pursued, or -1
     * @param lastCalcTick
     *            the tick when the path was computed
     */
    public void addPath(int entityId, java.util.List<Position> waypoints,
            int targetEntityId, long lastCalcTick) {
        Path p = paths[entityId];
        if (p == null) {
            p = new Path();
            paths[entityId] = p;
        }
        p.update(waypoints, targetEntityId, lastCalcTick);
        signatures[entityId] |= BIT_PATH;
    }

    // ── Typed remove methods (maintain signatures) ──

    /**
     * Removes the {@link AttackCooldown} component from an entity.
     *
     * @param entityId
     *            the entity ID
     */
    public void removeAttackCooldown(int entityId) {
        attackCooldowns[entityId] = null;
        signatures[entityId] &= ~BIT_ATTACK_COOLDOWN;
    }

    /**
     * Removes the {@link AttackIntent} component from an entity.
     *
     * @param entityId
     *            the entity ID
     */
    public void removeAttackIntent(int entityId) {
        attackIntents[entityId] = null;
        signatures[entityId] &= ~BIT_ATTACK_INTENT;
    }

    /**
     * Removes the {@link PortalCooldown} component from an entity.
     *
     * @param entityId
     *            the entity ID
     */
    public void removePortalCooldown(int entityId) {
        portalCooldowns[entityId] = null;
        signatures[entityId] &= ~BIT_PORTAL_COOLDOWN;
    }

    // ── Direct typed accessors (hot path) ──

    /**
     * Returns the Position array for direct indexed access.
     */
    public Position[] getPositions() {
        return positions;
    }

    /**
     * Returns the Velocity array for direct indexed access.
     */
    public Velocity[] getVelocities() {
        return velocities;
    }

    /**
     * Returns the Stats array for direct indexed access.
     */
    public Stats[] getStats() {
        return stats;
    }

    /**
     * Returns the BoundingBox array for direct indexed access.
     */
    public BoundingBox[] getBoundingBoxes() {
        return boundingBoxes;
    }

    /**
     * Returns the Dead array for direct indexed access.
     */
    public Dead[] getDeads() {
        return deads;
    }

    /**
     * Returns the Dirty array for direct indexed access.
     */
    public Dirty[] getDirties() {
        return dirties;
    }

    /**
     * Returns the Experience array for direct indexed access.
     */
    public Experience[] getExperiences() {
        return experiences;
    }

    /**
     * Returns the MovementIntent array for direct indexed access.
     */
    public MovementIntent[] getMovementIntents() {
        return movementIntents;
    }

    /**
     * Returns the Persistent array for direct indexed access.
     */
    public Persistent[] getPersistents() {
        return persistents;
    }

    /**
     * Returns the PlayerControlled array for direct indexed access.
     */
    public PlayerControlled[] getPlayerControlled() {
        return playerControlled;
    }

    /**
     * Returns the Portal array for direct indexed access.
     */
    public Portal[] getPortals() {
        return portals;
    }

    /**
     * Returns the PortalCooldown array for direct indexed access.
     */
    public PortalCooldown[] getPortalCooldowns() {
        return portalCooldowns;
    }

    /**
     * Returns the Renderable array for direct indexed access.
     */
    public Renderable[] getRenderables() {
        return renderables;
    }

    /**
     * Returns the Solid array for direct indexed access.
     */
    public Solid[] getSolids() {
        return solids;
    }

    /**
     * Returns the SpawnPoint array for direct indexed access.
     */
    public SpawnPoint[] getSpawnPoints() {
        return spawnPoints;
    }

    /**
     * Returns the Zone array for direct indexed access.
     */
    public Zone[] getZones() {
        return zones;
    }

    /**
     * Returns the AttackCooldown array for direct indexed access.
     */
    public AttackCooldown[] getAttackCooldowns() {
        return attackCooldowns;
    }

    /**
     * Returns the AttackIntent array for direct indexed access.
     */
    public AttackIntent[] getAttackIntents() {
        return attackIntents;
    }

    /**
     * Returns the Monster array for direct indexed access.
     */
    public Monster[] getMonsters() {
        return monsters;
    }

    /**
     * Returns the NpcAi array for direct indexed access.
     */
    public NpcAi[] getNpcAis() {
        return npcAis;
    }

    /**
     * Returns the Path array for direct indexed access.
     */
    public Path[] getPaths() {
        return paths;
    }

    // ── Generic component operations ──

    /**
     * Adds or replaces a component for an entity, updating the signature.
     *
     * @param entityId
     *            the entity ID (array index)
     * @param component
     *            the component
     */
    public void addComponent(int entityId, Component component) {
        Component[] arr = arrayMap.get(component.getClass());
        if (arr != null) {
            arr[entityId] = component;
            Long bit = BIT_MAP.get(component.getClass());
            if (bit != null) {
                signatures[entityId] |= bit;
            }
        }
    }

    /**
     * Gets a component for an entity.
     *
     * @param entityId
     *            the entity ID (array index)
     * @param componentClass
     *            the component class
     * @param <T>
     *            the component type
     * @return the component, or null if absent
     */
    @SuppressWarnings("unchecked")
    public <T extends Component> T getComponent(int entityId, Class<T> componentClass) {
        Component[] arr = arrayMap.get(componentClass);
        if (arr == null) {
            return null;
        }
        return (T) arr[entityId];
    }

    /**
     * Checks if an entity has a component.
     *
     * @param entityId
     *            the entity ID
     * @param componentClass
     *            the component class
     * @return true if the entity has the component
     */
    public boolean hasComponent(int entityId, Class<? extends Component> componentClass) {
        Component[] arr = arrayMap.get(componentClass);
        return arr != null && arr[entityId] != null;
    }

    /**
     * Removes a component from an entity, updating the signature.
     *
     * @param entityId
     *            the entity ID
     * @param componentClass
     *            the component class
     */
    public void removeComponent(int entityId, Class<? extends Component> componentClass) {
        Component[] arr = arrayMap.get(componentClass);
        if (arr != null) {
            arr[entityId] = null;
            Long bit = BIT_MAP.get(componentClass);
            if (bit != null) {
                signatures[entityId] &= ~bit;
            }
        }
    }

    /**
     * Removes all components for an entity and zeroes the signature.
     *
     * @param entityId
     *            the entity ID
     */
    public void removeAllComponents(int entityId) {
        // Defensively clear collections in mutable components before nulling
        // references, so recycled slots never inherit stale data.
        Path p = paths[entityId];
        if (p != null) {
            p.update(null, -1, 0);
        }
        for (Component[] arr : arrayMap.values()) {
            arr[entityId] = null;
        }
        signatures[entityId] = 0L;
    }

    /**
     * Gets all components for an entity.
     *
     * @param entityId
     *            the entity ID
     * @return map of component class to component instance
     */
    public Map<Class<? extends Component>, Component> getAllComponents(int entityId) {
        Map<Class<? extends Component>, Component> result = new HashMap<>();
        for (Map.Entry<Class<? extends Component>, Component[]> entry : arrayMap.entrySet()) {
            Component component = entry.getValue()[entityId];
            if (component != null) {
                result.put(entry.getKey(), component);
            }
        }
        return result;
    }
}
