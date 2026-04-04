package com.grimoire.testkit.fake;

import com.grimoire.application.core.port.GameEventPort;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Recording fake of {@link GameEventPort} for use in unit and integration
 * tests.
 */
public class FakeGameEventPort implements GameEventPort {

    private final List<DespawnEvent> recordedDespawnEvents = new ArrayList<>();
    private final List<ZoneChangeEvent> recordedZoneChangeEvents = new ArrayList<>();

    @Override
    public void onEntityDespawn(int entityId, String zoneId) {
        recordedDespawnEvents.add(new DespawnEvent(entityId, zoneId));
    }

    @Override
    public void onZoneChange(int entityId, String newZoneId, double x, double y) {
        recordedZoneChangeEvents.add(new ZoneChangeEvent(entityId, newZoneId, x, y));
    }

    /** Returns an unmodifiable view of all recorded despawn events. */
    public List<DespawnEvent> despawnEvents() {
        return Collections.unmodifiableList(recordedDespawnEvents);
    }

    /** Returns an unmodifiable view of all recorded zone-change events. */
    public List<ZoneChangeEvent> zoneChangeEvents() {
        return Collections.unmodifiableList(recordedZoneChangeEvents);
    }

    /** Returns the total number of events recorded across all types. */
    public int totalEventCount() {
        return recordedDespawnEvents.size() + recordedZoneChangeEvents.size();
    }

    /** Clears all recorded events. */
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
    public record DespawnEvent(int entityId, String zoneId) {
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
    public record ZoneChangeEvent(int entityId, String newZoneId, double x, double y) {
    }
}
