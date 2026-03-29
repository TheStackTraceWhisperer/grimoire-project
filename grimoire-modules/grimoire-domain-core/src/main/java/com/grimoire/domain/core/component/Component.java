package com.grimoire.domain.core.component;

/**
 * Base interface for all server-side ECS components.
 *
 * <p>
 * All components must be immutable records. Components are pure data — they
 * carry no behavior beyond what Java records provide (accessors,
 * equals/hashCode, toString).
 * </p>
 *
 * <p>
 * This interface is distinct from
 * {@link com.grimoire.contracts.api.component.ComponentDTO}, which marks
 * <em>wire-level</em> DTOs. Domain components are internal to the server and
 * are never serialized directly over the network.
 * </p>
 */
public interface Component {
}
