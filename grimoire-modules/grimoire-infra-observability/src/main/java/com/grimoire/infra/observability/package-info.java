/**
 * Observability infrastructure — Logback configuration and structured logging
 * utilities.
 *
 * <p>
 * This module provides:
 * </p>
 * <ul>
 * <li>A default {@code logback.xml} configuration with human-readable console
 * output and per-framework log level tuning.</li>
 * <li>{@link com.grimoire.infra.observability.Markers Markers} — centralised
 * SLF4J marker constants for subsystem-level log categorisation.</li>
 * </ul>
 *
 * <p>
 * Application modules that depend on this module automatically get the Logback
 * configuration on the classpath. JSON structured logging can be enabled in a
 * future wave by adding a profile-activated encoder.
 * </p>
 */
package com.grimoire.infra.observability;
