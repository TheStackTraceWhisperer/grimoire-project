package com.grimoire.client.state;

import com.grimoire.shared.component.ComponentDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A prefab is a template for creating entities with predefined components on the client.
 * 
 * <p><b>Thread Safety:</b> This class uses thread-safe collections internally
 * (CopyOnWriteArrayList) to allow safe concurrent access. Prefabs can be modified
 * after registration and accessed from multiple threads.</p>
 */
public class ClientPrefab {
    
    private final String name;
    private final List<ComponentDTO> componentTemplates;
    
    public ClientPrefab(String name) {
        this.name = name;
        this.componentTemplates = new CopyOnWriteArrayList<>();
    }
    
    /**
     * Adds a component template to this prefab.
     * @param component the component template
     * @return this prefab for chaining
     */
    public ClientPrefab addComponent(ComponentDTO component) {
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
    public List<ComponentDTO> getComponentTemplates() {
        return new ArrayList<>(componentTemplates);
    }
}
