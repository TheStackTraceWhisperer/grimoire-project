package com.grimoire.testkit.fake;

import com.grimoire.application.session.SessionConfig;

/**
 * Configurable fake of {@link SessionConfig} for use in tests.
 *
 * <p>
 * Provides a fixed session validity duration. Defaults to 30 minutes if
 * constructed with the no-arg constructor.
 * </p>
 */
public class FakeSessionConfig implements SessionConfig {

    /**
     * Default session validity in minutes.
     */
    private static final int DEFAULT_VALIDITY_MINUTES = 30;

    /**
     * Configured session validity in minutes.
     */
    private final int validityMinutes;

    /**
     * Creates a fake config with the default validity of 30 minutes.
     */
    public FakeSessionConfig() {
        this(DEFAULT_VALIDITY_MINUTES);
    }

    /**
     * Creates a fake config with a custom validity duration.
     *
     * @param validityMinutes
     *            session validity in minutes
     */
    public FakeSessionConfig(int validityMinutes) {
        this.validityMinutes = validityMinutes;
    }

    @Override
    public int sessionValidityMinutes() {
        return validityMinutes;
    }
}
