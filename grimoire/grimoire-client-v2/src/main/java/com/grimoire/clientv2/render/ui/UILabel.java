package com.grimoire.clientv2.render.ui;

import lombok.Getter;
import lombok.Setter;
import org.joml.Vector4f;

/**
 * Text label UI component.
 */
public class UILabel extends UIComponent {
    
    @Getter @Setter
    private String text;
    
    @Getter @Setter
    private Vector4f textColor;
    
    @Getter @Setter
    private float textScale;
    
    @Getter @Setter
    private boolean centered;
    
    /**
     * Creates a label with the specified text.
     */
    public UILabel(float x, float y, String text) {
        super(x, y, 100, 24);
        this.text = text;
        this.textColor = new Vector4f(1, 1, 1, 1);
        this.textScale = 1.0f;
        this.centered = false;
    }
    
    /**
     * Creates a label with specified dimensions.
     */
    public UILabel(float x, float y, float width, float height, String text) {
        super(x, y, width, height);
        this.text = text;
        this.textColor = new Vector4f(1, 1, 1, 1);
        this.textScale = 1.0f;
        this.centered = false;
    }
    
    /**
     * Creates a centered label.
     */
    public static UILabel centered(float x, float y, float width, float height, String text) {
        UILabel label = new UILabel(x, y, width, height, text);
        label.centered = true;
        return label;
    }
    
    @Override
    protected void renderContent(UIRenderer renderer) {
        if (text == null || text.isEmpty()) return;
        
        float absX = getAbsoluteX();
        float absY = getAbsoluteY();
        
        if (centered) {
            renderer.renderTextCentered(text, absX, absY, width, height, textScale, textColor);
        } else {
            // Render with some padding
            float textY = absY + (height - renderer.getFontRenderer().getTextHeight(textScale)) / 2;
            renderer.renderText(text, absX + padding, textY, textScale, textColor);
        }
    }
}
