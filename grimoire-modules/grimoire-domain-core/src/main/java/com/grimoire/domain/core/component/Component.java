package com.grimoire.domain.core.component;

/**
 * Base interface for all server-side ECS components.
 *
 * <p>
 * Components are mutable POJOs with public fields for zero-allocation updates.
 * They are stored in contiguous arrays inside the {@code ComponentManager} and
 * accessed by primitive {@code int} entity IDs.
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
