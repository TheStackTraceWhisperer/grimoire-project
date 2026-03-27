package com.grimoire.clientv2.render.ui;

import lombok.Getter;
import lombok.Setter;
import org.joml.Vector4f;

import java.util.function.Consumer;

/**
 * Clickable button UI component.
 */
public class UIButton extends UIComponent {
    
    @Getter @Setter
    private String text;
    
    @Getter @Setter
    private Vector4f textColor;
    
    @Getter @Setter
    private Vector4f hoverColor;
    
    @Getter @Setter
    private Vector4f pressedColor;
    
    @Getter @Setter
    private Vector4f disabledColor;
    
    @Getter
    private boolean hovered;
    
    @Getter
    private boolean pressed;
    
    private Consumer<UIButton> onClick;
    
    /**
     * Creates a button with the specified text.
     */
    public UIButton(float x, float y, float width, float height, String text) {
        super(x, y, width, height);
        this.text = text;
        this.backgroundColor = new Vector4f(0.2f, 0.4f, 0.6f, 1.0f);
        this.textColor = new Vector4f(1, 1, 1, 1);
        this.hoverColor = new Vector4f(0.3f, 0.5f, 0.7f, 1.0f);
        this.pressedColor = new Vector4f(0.15f, 0.3f, 0.45f, 1.0f);
        this.disabledColor = new Vector4f(0.3f, 0.3f, 0.3f, 1.0f);
    }
    
    /**
     * Sets the click handler.
     */
    public void setOnClick(Consumer<UIButton> handler) {
        this.onClick = handler;
    }
    
    /**
     * Sets the click handler (Runnable version).
     */
    public void setOnClick(Runnable handler) {
        this.onClick = btn -> handler.run();
    }
    
    @Override
    public void onMouseEnter() {
        hovered = true;
    }
    
    @Override
    public void onMouseExit() {
        hovered = false;
        pressed = false;
    }
    
    @Override
    public boolean onMouseClick(float mouseX, float mouseY, int button) {
        if (!visible || !enabled) return false;
        
        if (button == 0 && contains(mouseX, mouseY)) {
            if (onClick != null) {
                onClick.accept(this);
            }
            return true;
        }
        
        return super.onMouseClick(mouseX, mouseY, button);
    }
    
    @Override
    public boolean onMouseMove(float mouseX, float mouseY) {
        boolean wasHovered = hovered;
        hovered = contains(mouseX, mouseY);
        
        if (hovered && !wasHovered) {
            onMouseEnter();
        } else if (!hovered && wasHovered) {
            onMouseExit();
        }
        
        return hovered || super.onMouseMove(mouseX, mouseY);
    }
    
    @Override
    protected void renderContent(UIRenderer renderer) {
        float absX = getAbsoluteX();
        float absY = getAbsoluteY();
        
        // Determine button color based on state
        Vector4f buttonColor;
        if (!enabled) {
            buttonColor = disabledColor;
        } else if (pressed) {
            buttonColor = pressedColor;
        } else if (hovered) {
            buttonColor = hoverColor;
        } else {
            buttonColor = backgroundColor;
        }
        
        // Render button background (override the base class rendering)
        renderer.renderQuad(absX, absY, width, height, buttonColor);
        
        // Render border
        Vector4f borderColor = new Vector4f(buttonColor).mul(0.7f);
        borderColor.w = 1.0f;
        float borderWidth = 2.0f;
        
        renderer.renderQuad(absX, absY, width, borderWidth, borderColor);
        renderer.renderQuad(absX, absY + height - borderWidth, width, borderWidth, borderColor);
        renderer.renderQuad(absX, absY, borderWidth, height, borderColor);
        renderer.renderQuad(absX + width - borderWidth, absY, borderWidth, height, borderColor);
        
        // Render text
        if (text != null && !text.isEmpty()) {
            Vector4f actualTextColor = enabled ? textColor : new Vector4f(0.5f, 0.5f, 0.5f, 1.0f);
            renderer.renderTextCentered(text, absX, absY, width, height, 1.0f, actualTextColor);
        }
    }
    
    @Override
    public void render(UIRenderer renderer) {
        if (!visible) return;
        
        // Don't render the default background, let renderContent handle it
        renderContent(renderer);
        
        for (UIComponent child : children) {
            child.render(renderer);
        }
    }
}
