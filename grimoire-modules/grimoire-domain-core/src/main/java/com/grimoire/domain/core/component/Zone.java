package com.grimoire.domain.core.component;

/**
 * Zone component indicating which zone an entity belongs to.
 */
public class Zone implements Component {

    /**
     * The identifier of the zone.
     */
    public String zoneId;

    /**
     * No-arg constructor for array pre-allocation.
     */
    public Zone() {
        // default values
    }

    /**
     * Creates a zone component.
     *
     * @param zoneId
     *            the zone identifier
     */
    public Zone(String zoneId) {
        this.zoneId = zoneId;
    }

    /**
     * Zero-allocation update.
     *
     * @param newZoneId
     *            the new zone identifier
     */
    public void update(String newZoneId) {
        this.zoneId = newZoneId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Zone z)) {
            return false;
        }
        return java.util.Objects.equals(zoneId, z.zoneId);
    }

    @Override
    public int hashCode() {
        return zoneId != null ? zoneId.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Zone[zoneId=" + zoneId + "]";
    }
}
