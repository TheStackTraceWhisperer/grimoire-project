package com.grimoire.clientv2.state;

import com.grimoire.shared.component.ComponentDTO;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Client-side prefab for creating entities with predefined components.
 */
public class ClientPrefab {
    
    @Getter
    private final String name;
    
    private final List<ComponentDTO> componentTemplates;
    
    /**
     * Creates a new prefab with the given name.
     */
    public ClientPrefab(String name) {
        this.name = name;
        this.componentTemplates = new ArrayList<>();
    }
    
    /**
     * Adds a component template to this prefab.
     */
    public ClientPrefab addComponent(ComponentDTO component) {
        componentTemplates.add(component);
        return this;
    }
    
    /**
     * Gets the component templates.
     */
    public List<ComponentDTO> getComponentTemplates() {
        return Collections.unmodifiableList(componentTemplates);
    }
    
    /**
     * Creates a builder for this prefab.
     */
    public static Builder builder(String name) {
        return new Builder(name);
    }
    
    /**
     * Builder for creating prefabs.
     */
    public static class Builder {
        private final ClientPrefab prefab;
        
        private Builder(String name) {
            this.prefab = new ClientPrefab(name);
        }
        
        public Builder with(ComponentDTO component) {
            prefab.addComponent(component);
            return this;
        }
        
        public ClientPrefab build() {
            return prefab;
        }
    }
}
