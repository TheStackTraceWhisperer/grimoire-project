package com.grimoire.client.scene;

import com.grimoire.client.network.NetworkClient;
import com.grimoire.client.render.ui.*;
import com.grimoire.client.state.ClientEcsWorld;
import com.grimoire.shared.component.ComponentDTO;
import com.grimoire.shared.component.PositionDTO;
import com.grimoire.shared.component.RenderableDTO;
import com.grimoire.shared.dto.MovementIntent;
import com.grimoire.shared.protocol.GamePacket;
import com.grimoire.shared.protocol.PacketType;
import lombok.extern.slf4j.Slf4j;
import org.joml.Vector4f;

import java.util.Map;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Main game scene that renders the game world and entities.
 */
@Slf4j
public class GameScene extends Scene {
    
    // Rendering constants
    private static final float ENTITY_SIZE = 16.0f;
    private static final float GRID_SIZE = 40.0f;
    
    private final ClientEcsWorld ecsWorld;
    private final NetworkClient networkClient;
    
    private UIPanel uiPanel;
    private UILabel zoneLabel;
    private UILabel entityCountLabel;
    private UIButton disconnectButton;
    
    // Colors for different entity types
    private static final Vector4f COLOR_PLAYER = new Vector4f(0.39f, 0.78f, 1.0f, 1.0f);
    private static final Vector4f COLOR_NPC = new Vector4f(0.39f, 0.86f, 0.39f, 1.0f);
    private static final Vector4f COLOR_MONSTER_RAT = new Vector4f(0.55f, 0.35f, 0.17f, 1.0f);
    private static final Vector4f COLOR_MONSTER_WOLF = new Vector4f(0.51f, 0.51f, 0.55f, 1.0f);
    private static final Vector4f COLOR_MONSTER_BAT = new Vector4f(0.29f, 0.0f, 0.51f, 1.0f);
    private static final Vector4f COLOR_MONSTER_SKELETON = new Vector4f(0.86f, 0.86f, 0.86f, 1.0f);
    private static final Vector4f COLOR_PORTAL = new Vector4f(0.58f, 0.2f, 0.92f, 1.0f);
    private static final Vector4f COLOR_DEFAULT = new Vector4f(1.0f, 0.41f, 0.71f, 1.0f);
    
    private Runnable onDisconnect;
    
    public GameScene(ClientEcsWorld ecsWorld, NetworkClient networkClient) {
        this.ecsWorld = ecsWorld;
        this.networkClient = networkClient;
    }
    
    /**
     * Sets the disconnect callback.
     */
    public void setOnDisconnect(Runnable callback) {
        this.onDisconnect = callback;
    }
    
    @Override
    protected void onInit() {
        // Create UI elements
        createUI();
    }
    
    private void createUI() {
        // Top-left info panel
        uiPanel = new UIPanel(10, 10, 200, 80);
        
        zoneLabel = new UILabel(10, 10, 180, 24, "Zone: -");
        zoneLabel.setTextColor(new Vector4f(0.8f, 0.8f, 1.0f, 1.0f));
        uiPanel.addChild(zoneLabel);
        
        entityCountLabel = new UILabel(10, 40, 180, 24, "Entities: 0");
        entityCountLabel.setTextColor(new Vector4f(0.8f, 1.0f, 0.8f, 1.0f));
        uiPanel.addChild(entityCountLabel);
        
        // Disconnect button (bottom-left)
        disconnectButton = new UIButton(10, window.getHeight() - 50, 100, 40, "Disconnect");
        disconnectButton.setOnClick(() -> {
            if (onDisconnect != null) {
                onDisconnect.run();
            }
        });
    }
    
    @Override
    public void onEnter() {
        log.info("Entered game scene");
    }
    
    @Override
    public void onExit() {
        log.info("Exited game scene");
    }
    
    @Override
    public void update(float deltaTime) {
        // Update UI labels
        zoneLabel.setText("Zone: " + (ecsWorld.getCurrentZone() != null ? ecsWorld.getCurrentZone() : "-"));
        entityCountLabel.setText("Entities: " + ecsWorld.getEntityCount());
        
        // Handle input
        handleInput();
        
        // Update UI
        uiPanel.update(deltaTime);
        disconnectButton.update(deltaTime);
    }
    
    private void handleInput() {
        // Handle mouse click for movement
        if (inputManager.isMouseButtonPressed(GLFW_MOUSE_BUTTON_LEFT)) {
            double mouseX = inputManager.getMouseX();
            double mouseY = inputManager.getMouseY();
            
            // Check if clicking on UI
            if (!uiPanel.contains((float) mouseX, (float) mouseY) &&
                !disconnectButton.contains((float) mouseX, (float) mouseY)) {
                
                // Send movement intent to server
                MovementIntent intent = new MovementIntent(mouseX, mouseY);
                GamePacket packet = new GamePacket(PacketType.C2S_MOVEMENT_INTENT, intent);
                networkClient.send(packet);
                
                log.debug("Sent movement intent: ({}, {})", mouseX, mouseY);
            }
        }
        
        // Update UI hover states
        float mx = (float) inputManager.getMouseX();
        float my = (float) inputManager.getMouseY();
        uiPanel.onMouseMove(mx, my);
        disconnectButton.onMouseMove(mx, my);
        
        if (inputManager.isMouseButtonPressed(GLFW_MOUSE_BUTTON_LEFT)) {
            disconnectButton.onMouseClick(mx, my, 0);
        }
    }
    
    @Override
    public void render() {
        // Render world
        renderer.beginFrame();
        renderer.beginWorldRendering();
        
        // Render grid
        renderGrid();
        
        // Render entities
        renderEntities();
        
        renderer.endWorldRendering();
        
        // Render UI
        uiRenderer.begin();
        uiPanel.render(uiRenderer);
        disconnectButton.render(uiRenderer);
        uiRenderer.end();
        
        renderer.endFrame();
    }
    
    private void renderGrid() {
        Vector4f gridColor = new Vector4f(0.16f, 0.20f, 0.24f, 0.3f);
        
        int width = window.getWidth();
        int height = window.getHeight();
        
        // Vertical lines
        for (float x = 0; x < width; x += GRID_SIZE) {
            renderer.renderQuad(x, 0, 1, height, gridColor);
        }
        
        // Horizontal lines
        for (float y = 0; y < height; y += GRID_SIZE) {
            renderer.renderQuad(0, y, width, 1, gridColor);
        }
    }
    
    private void renderEntities() {
        for (Map.Entry<String, Map<Class<? extends ComponentDTO>, ComponentDTO>> entry : 
                ecsWorld.getEntities().entrySet()) {
            
            Map<Class<? extends ComponentDTO>, ComponentDTO> components = entry.getValue();
            
            PositionDTO pos = (PositionDTO) components.get(PositionDTO.class);
            RenderableDTO render = (RenderableDTO) components.get(RenderableDTO.class);
            
            if (pos != null && render != null) {
                Vector4f color = getEntityColor(render.visualId());
                float x = (float) pos.x();
                float y = (float) pos.y();
                
                // Render shadow
                Vector4f shadowColor = new Vector4f(0, 0, 0, 0.4f);
                renderer.renderQuad(x + 2, y + 2, ENTITY_SIZE, ENTITY_SIZE, shadowColor);
                
                // Render entity
                renderer.renderQuad(x, y, ENTITY_SIZE, ENTITY_SIZE, color);
                
                // Render highlight
                Vector4f highlightColor = new Vector4f(color).mul(1.3f);
                highlightColor.w = 1.0f;
                renderer.renderQuad(x + 3, y + 2, ENTITY_SIZE / 3, ENTITY_SIZE / 3, highlightColor);
            }
        }
    }
    
    private Vector4f getEntityColor(String visualId) {
        if (visualId == null) return COLOR_DEFAULT;
        
        return switch (visualId) {
            case "visual-player" -> COLOR_PLAYER;
            case "visual-npc-friendly" -> COLOR_NPC;
            case "visual-monster-rat" -> COLOR_MONSTER_RAT;
            case "visual-monster-wolf" -> COLOR_MONSTER_WOLF;
            case "visual-monster-bat" -> COLOR_MONSTER_BAT;
            case "visual-monster-skeleton" -> COLOR_MONSTER_SKELETON;
            case "visual-portal" -> COLOR_PORTAL;
            default -> COLOR_DEFAULT;
        };
    }
    
    @Override
    public void onResize(int width, int height) {
        // Reposition disconnect button
        if (disconnectButton != null) {
            disconnectButton.setY(height - 50);
        }
    }
    
    @Override
    public void cleanup() {
        if (uiPanel != null) uiPanel.cleanup();
        if (disconnectButton != null) disconnectButton.cleanup();
    }
}
