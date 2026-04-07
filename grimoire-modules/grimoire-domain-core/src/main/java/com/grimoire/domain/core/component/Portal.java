package com.grimoire.domain.core.component;

/**
 * Portal component for zone transitions.
 */
public class Portal implements Component {

    /**
     * The zone the portal leads to.
     */
    public String targetZoneId;

    /**
     * The exit portal identifier in the target zone.
     */
    public String targetPortalId;

    /**
     * No-arg constructor for array pre-allocation.
     */
    public Portal() {
        // default values
    }

    /**
     * Creates a portal component.
     *
     * @param targetZoneId
     *            the destination zone
     * @param targetPortalId
     *            the exit portal name
     */
    public Portal(String targetZoneId, String targetPortalId) {
        this.targetZoneId = targetZoneId;
        this.targetPortalId = targetPortalId;
    }

    /**
     * Zero-allocation update.
     *
     * @param newTargetZoneId
     *            new destination zone
     * @param newTargetPortalId
     *            new exit portal name
     */
    public void update(String newTargetZoneId, String newTargetPortalId) {
        this.targetZoneId = newTargetZoneId;
        this.targetPortalId = newTargetPortalId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Portal p)) {
            return false;
        }
        return java.util.Objects.equals(targetZoneId, p.targetZoneId)
                && java.util.Objects.equals(targetPortalId, p.targetPortalId);
    }

    @Override
    public int hashCode() {
        int result = targetZoneId != null ? targetZoneId.hashCode() : 0;
        return 31 * result + (targetPortalId != null ? targetPortalId.hashCode() : 0);
    }

    @Override
    public String toString() {
        return "Portal[targetZoneId=" + targetZoneId + ", targetPortalId=" + targetPortalId + "]";
    }
}
