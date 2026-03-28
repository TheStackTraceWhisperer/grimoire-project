package com.grimoire.server.component;
import com.grimoire.ecs.Component;

/**
 * Component storing an NPC's spawn point and leash radius.
 * 
 * <p>Used to implement anti-kiting mechanics by anchoring monsters to their
 * spawn location. When an NPC moves beyond the leash radius, it will stop
 * pursuing targets and return to its spawn point.</p>
 * 
 * @param x the X coordinate of the spawn point
 * @param y the Y coordinate of the spawn point
 * @param leashRadius the maximum distance from spawn point before the NPC resets
 */
public record SpawnPoint(double x, double y, double leashRadius) implements Component {
}
