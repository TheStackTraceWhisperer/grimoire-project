package com.grimoire.infra.observability;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * Centralised SLF4J {@link Marker} constants for structured logging.
 *
 * <p>
 * Markers allow log statements to be categorised by subsystem without changing
 * the logger name. Appenders can filter or route messages based on markers,
 * enabling per-subsystem log files or structured JSON fields.
 * </p>
 *
 * <p>
 * Usage:
 * </p>
 *
 * <pre>{@code
 * import static com.grimoire.infra.observability.Markers.NETWORK;
 * LOG.info(NETWORK, "Client connected: {}", sessionId);
 * }</pre>
 */
public final class Markers {

    /**
     * Marker for network-related log messages (Netty, codecs, channels).
     */
    public static final Marker NETWORK = MarkerFactory.getMarker("NETWORK");

    /**
     * Marker for session lifecycle log messages (login, logout, expiry).
     */
    public static final Marker SESSION = MarkerFactory.getMarker("SESSION");

    /**
     * Marker for combat system log messages (attacks, damage, death).
     */
    public static final Marker COMBAT = MarkerFactory.getMarker("COMBAT");

    /**
     * Marker for persistence operations (JPA, database).
     */
    public static final Marker PERSISTENCE = MarkerFactory.getMarker("PERSISTENCE");

    /**
     * Marker for ECS engine log messages (ticks, systems, entities).
     */
    public static final Marker ENGINE = MarkerFactory.getMarker("ENGINE");

    /**
     * Marker for navigation and zone-change log messages.
     */
    public static final Marker NAVIGATION = MarkerFactory.getMarker("NAVIGATION");

    private Markers() {
        // utility class
    }
}
