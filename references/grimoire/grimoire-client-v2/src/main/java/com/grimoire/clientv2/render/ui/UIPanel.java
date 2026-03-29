package com.grimoire.clientv2.render.ui;

import org.joml.Vector4f;

/**
 * Simple panel component for grouping other UI elements.
 */
public class UIPanel extends UIComponent {
    
    private Vector4f borderColor;
    private float borderWidth;
    
    /**
     * Creates a panel at the specified position with the given size.
     */
    public UIPanel(float x, float y, float width, float height) {
        super(x, y, width, height);
        this.backgroundColor = new Vector4f(0.15f, 0.15f, 0.2f, 0.9f);
        this.borderColor = new Vector4f(0.3f, 0.3f, 0.4f, 1.0f);
        this.borderWidth = 2.0f;
    }
    
    /**
     * Creates a transparent panel (for grouping only).
     */
    public UIPanel(float x, float y, float width, float height, boolean transparent) {
        super(x, y, width, height);
        if (transparent) {
            this.backgroundColor = new Vector4f(0, 0, 0, 0);
            this.borderColor = null;
            this.borderWidth = 0;
        } else {
            this.backgroundColor = new Vector4f(0.15f, 0.15f, 0.2f, 0.9f);
            this.borderColor = new Vector4f(0.3f, 0.3f, 0.4f, 1.0f);
            this.borderWidth = 2.0f;
        }
    }
    
    /**
     * Sets the border color.
     */
    public void setBorderColor(Vector4f color) {
        this.borderColor = color;
    }
    
    /**
     * Sets the border width.
     */
    public void setBorderWidth(float width) {
        this.borderWidth = width;
    }
    
    @Override
    protected void renderContent(UIRenderer renderer) {
        // Render border if present
        if (borderColor != null && borderWidth > 0) {
            float x = getAbsoluteX();
            float y = getAbsoluteY();
            
            // Top border
            renderer.renderQuad(x, y, width, borderWidth, borderColor);
            // Bottom border
            renderer.renderQuad(x, y + height - borderWidth, width, borderWidth, borderColor);
            // Left border
            renderer.renderQuad(x, y, borderWidth, height, borderColor);
            // Right border
            renderer.renderQuad(x + width - borderWidth, y, borderWidth, height, borderColor);
        }
    }
}
