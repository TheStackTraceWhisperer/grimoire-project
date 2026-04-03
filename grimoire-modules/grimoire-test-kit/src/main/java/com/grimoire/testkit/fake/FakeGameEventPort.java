package com.grimoire.testkit.fake;

import com.grimoire.application.core.port.GameEventPort;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Recording fake of {@link GameEventPort} for use in unit and integration
 * tests.
 *
 * <p>
 * Captures all event invocations in ordered lists that can be queried after a
 * test tick. Call {@link #clear()} between test cases if the same instance is
 * reused.
 * </p>
 */
public class FakeGameEventPort implements GameEventPort {

    /** Recorded entity-despawn events. */
    private final List<DespawnEvent> recordedDespawnEvents = new ArrayList<>();

    /** Recorded zone-change events. */
    private final List<ZoneChangeEvent> recordedZoneChangeEvents = new ArrayList<>();

    @Override
    public void onEntityDespawn(String entityId, String zoneId) {
        recordedDespawnEvents.add(new DespawnEvent(entityId, zoneId));
    }

    @Override
    public void onZoneChange(String entityId, String newZoneId, double x, double y) {
        recordedZoneChangeEvents.add(new ZoneChangeEvent(entityId, newZoneId, x, y));
    }

    /**
     * Returns an unmodifiable view of all recorded despawn events.
     *
     * @return despawn event list
     */
    public List<DespawnEvent> despawnEvents() {
        return Collections.unmodifiableList(recordedDespawnEvents);
    }

    /**
     * Returns an unmodifiable view of all recorded zone-change events.
     *
     * @return zone-change event list
     */
    public List<ZoneChangeEvent> zoneChangeEvents() {
        return Collections.unmodifiableList(recordedZoneChangeEvents);
    }

    /**
     * Returns the total number of events recorded across all types.
     *
     * @return total event count
     */
    public int totalEventCount() {
        return recordedDespawnEvents.size() + recordedZoneChangeEvents.size();
    }

    /**
     * Clears all recorded events.
     */
    public void clear() {
        recordedDespawnEvents.clear();
        recordedZoneChangeEvents.clear();
    }

    /**
     * A recorded entity-despawn event.
     *
     * @param entityId
     *            the despawned entity's ID
     * @param zoneId
     *            the zone in which the despawn occurred
     */
    public record DespawnEvent(String entityId, String zoneId) {
    }

    /**
     * A recorded zone-change event.
     *
     * @param entityId
     *            the entity that changed zones
     * @param newZoneId
     *            the target zone
     * @param x
     *            the new X position
     * @param y
     *            the new Y position
     */
    public record ZoneChangeEvent(String entityId, String newZoneId, double x, double y) {
    }
}
