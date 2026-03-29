package com.grimoire.application.session;

/**
 * Configuration port for session policy parameters.
 *
 * <p>
 * Infrastructure adapters provide the implementation, typically backed by
 * external configuration (e.g., {@code @ConfigurationProperties}).
 * </p>
 */
@FunctionalInterface
public interface SessionConfig {

    /**
     * How long a session remains valid after creation.
     *
     * @return validity duration in minutes
     */
    int sessionValidityMinutes();
}
