package com.grimoire.server.ecs;

import com.grimoire.server.component.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A prefab is a template for creating entities with predefined components.
 * Components are stored as templates and can be customized when instantiating.
 * 
 * <p><b>Component Instance Sharing:</b> Since components are immutable records,
 * component instances are shared across all entities created from the same prefab
 * when no customization is applied. This is safe due to immutability and reduces
 * memory usage, but should be considered when reasoning about memory patterns.</p>
 * 
 * <p><b>Thread Safety:</b> This class uses thread-safe collections internally
 * (CopyOnWriteArrayList) to allow safe concurrent access. Prefabs can be modified
 * after registration and accessed from multiple threads.</p>
 */
public class Prefab {
    
    private final String name;
    private final List<Component> componentTemplates;
    
    public Prefab(String name) {
        this.name = name;
        this.componentTemplates = new CopyOnWriteArrayList<>();
    }
    
    /**
     * Adds a component template to this prefab.
     * @param component the component template
     * @return this prefab for chaining
     */
    public Prefab addComponent(Component component) {
        componentTemplates.add(component);
        return this;
    }
    
    /**
     * Gets the name of this prefab.
     * @return prefab name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Gets all component templates for this prefab.
     * @return list of component templates
     */
    public List<Component> getComponentTemplates() {
        return new ArrayList<>(componentTemplates);
    }
}
