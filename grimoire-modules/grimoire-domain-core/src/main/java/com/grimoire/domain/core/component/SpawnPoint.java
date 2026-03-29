package com.grimoire.domain.core.component;

/**
 * Spawn point component storing an NPC's origin and leash radius.
 *
 * <p>
 * Used to implement anti-kiting mechanics by anchoring monsters to their spawn
 * location. When an NPC moves beyond the leash radius, it stops pursuing
 * targets and returns to its spawn point.
 * </p>
 *
 * @param x
 *            the X coordinate of the spawn point
 * @param y
 *            the Y coordinate of the spawn point
 * @param leashRadius
 *            the maximum distance from the spawn point before the NPC resets
 */
public record SpawnPoint(double x, double y, double leashRadius) implements Component {
}
