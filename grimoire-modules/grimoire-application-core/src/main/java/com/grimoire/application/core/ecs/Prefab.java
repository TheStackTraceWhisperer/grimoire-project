package com.grimoire.application.core.ecs;

import com.grimoire.domain.core.component.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * A prefab is a template for creating entities with predefined components.
 *
 * <p>
 * Components are stored as templates and can be customised when instantiating.
 * Since domain components are immutable records, template instances are shared
 * across all entities created from the same prefab (unless a customiser
 * replaces them).
 * </p>
 */
public class Prefab {

    /** Human-readable name of this prefab. */
    private final String name;

    /** Ordered list of component templates to apply on entity creation. */
    private final List<Component> componentTemplates;

    /**
     * Creates a new prefab with the given name.
     *
     * @param name
     *            the prefab name
     */
    public Prefab(String name) {
        this.name = name;
        this.componentTemplates = new ArrayList<>();
    }

    /**
     * Adds a component template to this prefab.
     *
     * @param component
     *            the component template
     * @return this prefab for chaining
     */
    public Prefab addComponent(Component component) {
        componentTemplates.add(component);
        return this;
    }

    /**
     * Gets the name of this prefab.
     *
     * @return prefab name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets all component templates for this prefab.
     *
     * @return defensive copy of the component template list
     */
    public List<Component> getComponentTemplates() {
        return new ArrayList<>(componentTemplates);
    }
}
