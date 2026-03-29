package com.grimoire.contracts.api.component;

import java.io.Serializable;

/**
 * Base interface for all component DTOs sent over the network.
 *
 * <p>
 * Component DTOs are lightweight, serializable snapshots of server-side
 * component state, suitable for wire transport to clients.
 * </p>
 */
public interface ComponentDTO extends Serializable {
}
