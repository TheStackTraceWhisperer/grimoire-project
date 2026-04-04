package com.grimoire.application.core.ecs;

import com.grimoire.domain.combat.component.AttackCooldown;
import com.grimoire.domain.combat.component.AttackIntent;
import com.grimoire.domain.combat.component.Monster;
import com.grimoire.domain.combat.component.NpcAi;
import com.grimoire.domain.core.component.BoundingBox;
import com.grimoire.domain.core.component.Component;
import com.grimoire.domain.core.component.Dead;
import com.grimoire.domain.core.component.Dirty;
import com.grimoire.domain.core.component.Experience;
import com.grimoire.domain.core.component.MovementIntent;
import com.grimoire.domain.core.component.Persistent;
import com.grimoire.domain.core.component.PlayerControlled;
import com.grimoire.domain.core.component.Portal;
import com.grimoire.domain.core.component.PortalCooldown;
import com.grimoire.domain.core.component.Position;
import com.grimoire.domain.core.component.Renderable;
import com.grimoire.domain.core.component.Solid;
import com.grimoire.domain.core.component.SpawnPoint;
import com.grimoire.domain.core.component.Stats;
import com.grimoire.domain.core.component.Velocity;
import com.grimoire.domain.core.component.Zone;
import com.grimoire.domain.navigation.component.Path;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages component data using fixed-size contiguous arrays.
 *
 * <p>
 * Each component type has a dedicated array of size
 * {@link EntityManager#MAX_ENTITIES}. Entity IDs are used directly as array
 * indices. A {@code null} entry means the entity does not have that component.
 * </p>
 *
 * <p>
 * <strong>Thread-safety:</strong> This class must only be accessed from the
 * single-threaded game loop.
 * </p>
 */
@SuppressWarnings({"PMD.CouplingBetweenObjects", "PMD.DataClass"})
public class ComponentManager {

    private static final int MAX = EntityManager.MAX_ENTITIES;

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

    /** Initializes the component-type-to-array mapping. */
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

    // ── Direct typed accessors (hot path) ──

    /** Returns the Position array for direct indexed access. */
    public Position[] getPositions() {
        return positions;
    }

    /** Returns the Velocity array for direct indexed access. */
    public Velocity[] getVelocities() {
        return velocities;
    }

    /** Returns the Stats array for direct indexed access. */
    public Stats[] getStats() {
        return stats;
    }

    /** Returns the BoundingBox array for direct indexed access. */
    public BoundingBox[] getBoundingBoxes() {
        return boundingBoxes;
    }

    /** Returns the Dead array for direct indexed access. */
    public Dead[] getDeads() {
        return deads;
    }

    /** Returns the Dirty array for direct indexed access. */
    public Dirty[] getDirties() {
        return dirties;
    }

    /** Returns the Experience array for direct indexed access. */
    public Experience[] getExperiences() {
        return experiences;
    }

    /** Returns the MovementIntent array for direct indexed access. */
    public MovementIntent[] getMovementIntents() {
        return movementIntents;
    }

    /** Returns the Persistent array for direct indexed access. */
    public Persistent[] getPersistents() {
        return persistents;
    }

    /** Returns the PlayerControlled array for direct indexed access. */
    public PlayerControlled[] getPlayerControlled() {
        return playerControlled;
    }

    /** Returns the Portal array for direct indexed access. */
    public Portal[] getPortals() {
        return portals;
    }

    /** Returns the PortalCooldown array for direct indexed access. */
    public PortalCooldown[] getPortalCooldowns() {
        return portalCooldowns;
    }

    /** Returns the Renderable array for direct indexed access. */
    public Renderable[] getRenderables() {
        return renderables;
    }

    /** Returns the Solid array for direct indexed access. */
    public Solid[] getSolids() {
        return solids;
    }

    /** Returns the SpawnPoint array for direct indexed access. */
    public SpawnPoint[] getSpawnPoints() {
        return spawnPoints;
    }

    /** Returns the Zone array for direct indexed access. */
    public Zone[] getZones() {
        return zones;
    }

    /** Returns the AttackCooldown array for direct indexed access. */
    public AttackCooldown[] getAttackCooldowns() {
        return attackCooldowns;
    }

    /** Returns the AttackIntent array for direct indexed access. */
    public AttackIntent[] getAttackIntents() {
        return attackIntents;
    }

    /** Returns the Monster array for direct indexed access. */
    public Monster[] getMonsters() {
        return monsters;
    }

    /** Returns the NpcAi array for direct indexed access. */
    public NpcAi[] getNpcAis() {
        return npcAis;
    }

    /** Returns the Path array for direct indexed access. */
    public Path[] getPaths() {
        return paths;
    }

    // ── Generic component operations ──

    /**
     * Adds or replaces a component for an entity.
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
     * Removes a component from an entity.
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
        }
    }

    /**
     * Removes all components for an entity.
     *
     * @param entityId
     *            the entity ID
     */
    public void removeAllComponents(int entityId) {
        for (Component[] arr : arrayMap.values()) {
            arr[entityId] = null;
        }
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
