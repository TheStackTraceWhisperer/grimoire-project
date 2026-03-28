package com.grimoire.client.render.ui;

import lombok.Getter;
import lombok.Setter;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for all UI components.
 * Provides positioning, sizing, and hierarchical structure.
 */
public abstract class UIComponent {
    
    @Getter @Setter
    protected float x;
    
    @Getter @Setter
    protected float y;
    
    @Getter @Setter
    protected float width;
    
    @Getter @Setter
    protected float height;
    
    @Getter @Setter
    protected boolean visible = true;
    
    @Getter @Setter
    protected boolean enabled = true;
    
    @Getter
    protected UIComponent parent;
    
    @Getter
    protected final List<UIComponent> children = new ArrayList<>();
    
    @Getter @Setter
    protected Vector4f backgroundColor = new Vector4f(0, 0, 0, 0);
    
    @Getter @Setter
    protected float padding = 0;
    
    @Getter @Setter
    protected float margin = 0;
    
    /**
     * Creates a UI component at the specified position with the given size.
     */
    public UIComponent(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    
    /**
     * Adds a child component.
     */
    public void addChild(UIComponent child) {
        child.parent = this;
        children.add(child);
    }
    
    /**
     * Removes a child component.
     */
    public void removeChild(UIComponent child) {
        children.remove(child);
        child.parent = null;
    }
    
    /**
     * Gets the absolute X position (accounting for parent positions).
     */
    public float getAbsoluteX() {
        float absX = x;
        if (parent != null) {
            absX += parent.getAbsoluteX() + parent.padding;
        }
        return absX;
    }
    
    /**
     * Gets the absolute Y position (accounting for parent positions).
     */
    public float getAbsoluteY() {
        float absY = y;
        if (parent != null) {
            absY += parent.getAbsoluteY() + parent.padding;
        }
        return absY;
    }
    
    /**
     * Checks if a point is inside this component.
     */
    public boolean contains(float px, float py) {
        float absX = getAbsoluteX();
        float absY = getAbsoluteY();
        return px >= absX && px <= absX + width && py >= absY && py <= absY + height;
    }
    
    /**
     * Sets the bounds of this component.
     */
    public void setBounds(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    
    /**
     * Sets the position of this component.
     */
    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }
    
    /**
     * Sets the size of this component.
     */
    public void setSize(float width, float height) {
        this.width = width;
        this.height = height;
    }
    
    /**
     * Updates the component (called each frame).
     */
    public void update(float deltaTime) {
        if (!visible) return;
        
        for (UIComponent child : children) {
            child.update(deltaTime);
        }
    }
    
    /**
     * Renders the component.
     */
    public void render(UIRenderer renderer) {
        if (!visible) return;
        
        // Render background
        if (backgroundColor.w > 0) {
            renderer.renderQuad(getAbsoluteX(), getAbsoluteY(), width, height, backgroundColor);
        }
        
        // Render this component's content
        renderContent(renderer);
        
        // Render children
        for (UIComponent child : children) {
            child.render(renderer);
        }
    }
    
    /**
     * Renders the component-specific content.
     */
    protected abstract void renderContent(UIRenderer renderer);
    
    /**
     * Handles mouse click events.
     * 
     * @param mouseX Mouse X position
     * @param mouseY Mouse Y position
     * @param button Mouse button (0=left, 1=right, 2=middle)
     * @return true if the event was handled
     */
    public boolean onMouseClick(float mouseX, float mouseY, int button) {
        if (!visible || !enabled) return false;
        
        // Check children first (top-most first)
        for (int i = children.size() - 1; i >= 0; i--) {
            if (children.get(i).onMouseClick(mouseX, mouseY, button)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Handles mouse move events.
     */
    public boolean onMouseMove(float mouseX, float mouseY) {
        if (!visible) return false;
        
        for (int i = children.size() - 1; i >= 0; i--) {
            if (children.get(i).onMouseMove(mouseX, mouseY)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Handles mouse enter events.
     */
    public void onMouseEnter() {
        // Override in subclasses
    }
    
    /**
     * Handles mouse exit events.
     */
    public void onMouseExit() {
        // Override in subclasses
    }
    
    /**
     * Cleans up component resources.
     */
    public void cleanup() {
        for (UIComponent child : children) {
            child.cleanup();
        }
    }
}
