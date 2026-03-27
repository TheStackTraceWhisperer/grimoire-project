package com.grimoire.client.ui;

import com.grimoire.client.network.NetworkClient;
import com.grimoire.client.state.ClientEcsWorld;
import com.grimoire.shared.component.ComponentDTO;
import com.grimoire.shared.component.PositionDTO;
import com.grimoire.shared.component.RenderableDTO;
import com.grimoire.shared.dto.MovementIntent;
import com.grimoire.shared.protocol.GamePacket;
import com.grimoire.shared.protocol.PacketType;
import jakarta.inject.Singleton;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * Controller for the main game screen.
 */
@Singleton
@Slf4j
public class GameController {
    
    // Rendering constants
    private static final int GRID_SIZE = 40;
    private static final double ENTITY_SIZE = 16.0;
    private static final double SHADOW_OFFSET = 2.0;
    private static final double HIGHLIGHT_OFFSET_X = 3.0;
    private static final double HIGHLIGHT_OFFSET_Y = 2.0;
    private static final double HIGHLIGHT_SIZE_RATIO = 1.0 / 3.0;
    private static final double TEXT_OFFSET_X = -11.0;
    private static final double TEXT_OFFSET_Y = -9.0;
    private static final double TEXT_SHADOW_OFFSET_X = 1.0;
    private static final double TEXT_SHADOW_OFFSET_Y = 1.0;
    
    // Cached colors to avoid GC pressure in render loop
    private static final Color BACKGROUND_COLOR = Color.rgb(26, 35, 45);
    private static final Color GRID_COLOR = Color.rgb(40, 50, 60, 0.3);
    private static final Color SHADOW_COLOR = Color.rgb(0, 0, 0, 0.4);
    private static final Color TEXT_SHADOW_COLOR = Color.rgb(0, 0, 0, 0.6);
    private static final Color TEXT_COLOR = Color.rgb(255, 255, 255, 0.95);
    
    // Cached entity colors
    private static final Color COLOR_PLAYER = Color.rgb(100, 200, 255);
    private static final Color COLOR_PLAYER_HIGHLIGHT = COLOR_PLAYER.brighter().brighter();
    private static final Color COLOR_PLAYER_OUTLINE = COLOR_PLAYER.darker();
    
    private static final Color COLOR_NPC_FRIENDLY = Color.rgb(100, 220, 100);
    private static final Color COLOR_NPC_FRIENDLY_HIGHLIGHT = COLOR_NPC_FRIENDLY.brighter().brighter();
    private static final Color COLOR_NPC_FRIENDLY_OUTLINE = COLOR_NPC_FRIENDLY.darker();
    
    private static final Color COLOR_MONSTER_RAT = Color.rgb(139, 90, 43);
    private static final Color COLOR_MONSTER_RAT_HIGHLIGHT = COLOR_MONSTER_RAT.brighter().brighter();
    private static final Color COLOR_MONSTER_RAT_OUTLINE = COLOR_MONSTER_RAT.darker();
    
    private static final Color COLOR_MONSTER_WOLF = Color.rgb(130, 130, 140);
    private static final Color COLOR_MONSTER_WOLF_HIGHLIGHT = COLOR_MONSTER_WOLF.brighter().brighter();
    private static final Color COLOR_MONSTER_WOLF_OUTLINE = COLOR_MONSTER_WOLF.darker();
    
    private static final Color COLOR_MONSTER_BAT = Color.rgb(75, 0, 130);
    private static final Color COLOR_MONSTER_BAT_HIGHLIGHT = COLOR_MONSTER_BAT.brighter().brighter();
    private static final Color COLOR_MONSTER_BAT_OUTLINE = COLOR_MONSTER_BAT.darker();
    
    private static final Color COLOR_MONSTER_SKELETON = Color.rgb(220, 220, 220);
    private static final Color COLOR_MONSTER_SKELETON_HIGHLIGHT = COLOR_MONSTER_SKELETON.brighter().brighter();
    private static final Color COLOR_MONSTER_SKELETON_OUTLINE = COLOR_MONSTER_SKELETON.darker();
    
    private static final Color COLOR_PORTAL = Color.rgb(147, 51, 234);
    private static final Color COLOR_PORTAL_HIGHLIGHT = COLOR_PORTAL.brighter().brighter();
    private static final Color COLOR_PORTAL_OUTLINE = COLOR_PORTAL.darker();
    
    private static final Color COLOR_ERROR = Color.rgb(255, 105, 180);
    private static final Color COLOR_ERROR_HIGHLIGHT = COLOR_ERROR.brighter().brighter();
    private static final Color COLOR_ERROR_OUTLINE = COLOR_ERROR.darker();
    
    // Cached font to avoid GC pressure in render loop
    // Uses system default font family for cross-platform compatibility
    private static final Font ENTITY_NAME_FONT = Font.font(Font.getDefault().getFamily(), FontWeight.BOLD, 12);
    
    @FXML
    private Canvas gameCanvas;
    
    private final ClientEcsWorld clientEcsWorld;
    private final NetworkClient networkClient;
    private AnimationTimer renderLoop;
    
    public GameController(ClientEcsWorld clientEcsWorld, NetworkClient networkClient) {
        this.clientEcsWorld = clientEcsWorld;
        this.networkClient = networkClient;
    }
    
    @FXML
    private void initialize() {
        log.info("Game controller initialized");
        
        // Set up canvas click handler for movement
        gameCanvas.setOnMouseClicked(this::handleCanvasClick);
        
        // Start render loop
        startRenderLoop();
    }
    
    private void handleCanvasClick(MouseEvent event) {
        double x = event.getX();
        double y = event.getY();
        
        log.debug("Canvas clicked at ({}, {})", x, y);
        
        MovementIntent intent = new MovementIntent(x, y);
        GamePacket packet = new GamePacket(PacketType.C2S_MOVEMENT_INTENT, intent);
        networkClient.send(packet);
    }
    
    private void startRenderLoop() {
        renderLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                render();
            }
        };
        renderLoop.start();
    }
    
    private void render() {
        GraphicsContext gc = gameCanvas.getGraphicsContext2D();
        
        // Clear screen with gradient background for depth
        gc.setFill(BACKGROUND_COLOR);
        gc.fillRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());
        
        // Add subtle grid pattern for visual richness
        gc.setStroke(GRID_COLOR);
        gc.setLineWidth(0.5);
        for (int x = 0; x < gameCanvas.getWidth(); x += GRID_SIZE) {
            gc.strokeLine(x, 0, x, gameCanvas.getHeight());
        }
        for (int y = 0; y < gameCanvas.getHeight(); y += GRID_SIZE) {
            gc.strokeLine(0, y, gameCanvas.getWidth(), y);
        }
        
        // Render all entities with enhanced visuals
        for (Map.Entry<String, Map<Class<? extends ComponentDTO>, ComponentDTO>> entry : 
                clientEcsWorld.getEntities().entrySet()) {
            
            Map<Class<? extends ComponentDTO>, ComponentDTO> components = entry.getValue();
            
            if (components.get(PositionDTO.class) instanceof PositionDTO pos &&
                components.get(RenderableDTO.class) instanceof RenderableDTO render) {
                
                String visual = render.visualId();
                double x = pos.x();
                double y = pos.y();
                
                // Draw shadow for depth
                gc.setFill(SHADOW_COLOR);
                gc.fillOval(x + SHADOW_OFFSET, y + SHADOW_OFFSET, ENTITY_SIZE, ENTITY_SIZE);
                
                // Get cached colors for entity type
                Color entityColor = getEntityColor(visual);
                Color highlightColor = getEntityHighlightColor(visual);
                Color outlineColor = getEntityOutlineColor(visual);
                
                gc.setFill(entityColor);
                gc.fillOval(x, y, ENTITY_SIZE, ENTITY_SIZE);
                
                // Add highlight for dimension
                gc.setFill(highlightColor);
                gc.fillOval(x + HIGHLIGHT_OFFSET_X, y + HIGHLIGHT_OFFSET_Y, 
                           ENTITY_SIZE * HIGHLIGHT_SIZE_RATIO, ENTITY_SIZE * HIGHLIGHT_SIZE_RATIO);
                
                // Draw outline for definition
                gc.setStroke(outlineColor);
                gc.setLineWidth(1.5);
                gc.strokeOval(x, y, ENTITY_SIZE, ENTITY_SIZE);
                
                // Draw name with shadow for readability
                gc.setFont(ENTITY_NAME_FONT);
                gc.setFill(TEXT_SHADOW_COLOR);
                gc.fillText(render.name(), x + TEXT_OFFSET_X + TEXT_SHADOW_OFFSET_X, 
                           y + TEXT_OFFSET_Y + TEXT_SHADOW_OFFSET_Y);
                gc.setFill(TEXT_COLOR);
                gc.fillText(render.name(), x + TEXT_OFFSET_X, y + TEXT_OFFSET_Y);
            }
        }
    }
    
    private static Color getEntityColor(String visualId) {
        return switch (visualId) {
            case "visual-player" -> COLOR_PLAYER;
            case "visual-npc-friendly" -> COLOR_NPC_FRIENDLY;
            case "visual-monster-rat" -> COLOR_MONSTER_RAT;
            case "visual-monster-wolf" -> COLOR_MONSTER_WOLF;
            case "visual-monster-bat" -> COLOR_MONSTER_BAT;
            case "visual-monster-skeleton" -> COLOR_MONSTER_SKELETON;
            case "visual-portal" -> COLOR_PORTAL;
            default -> COLOR_ERROR;
        };
    }
    
    private static Color getEntityHighlightColor(String visualId) {
        return switch (visualId) {
            case "visual-player" -> COLOR_PLAYER_HIGHLIGHT;
            case "visual-npc-friendly" -> COLOR_NPC_FRIENDLY_HIGHLIGHT;
            case "visual-monster-rat" -> COLOR_MONSTER_RAT_HIGHLIGHT;
            case "visual-monster-wolf" -> COLOR_MONSTER_WOLF_HIGHLIGHT;
            case "visual-monster-bat" -> COLOR_MONSTER_BAT_HIGHLIGHT;
            case "visual-monster-skeleton" -> COLOR_MONSTER_SKELETON_HIGHLIGHT;
            case "visual-portal" -> COLOR_PORTAL_HIGHLIGHT;
            default -> COLOR_ERROR_HIGHLIGHT;
        };
    }
    
    private static Color getEntityOutlineColor(String visualId) {
        return switch (visualId) {
            case "visual-player" -> COLOR_PLAYER_OUTLINE;
            case "visual-npc-friendly" -> COLOR_NPC_FRIENDLY_OUTLINE;
            case "visual-monster-rat" -> COLOR_MONSTER_RAT_OUTLINE;
            case "visual-monster-wolf" -> COLOR_MONSTER_WOLF_OUTLINE;
            case "visual-monster-bat" -> COLOR_MONSTER_BAT_OUTLINE;
            case "visual-monster-skeleton" -> COLOR_MONSTER_SKELETON_OUTLINE;
            case "visual-portal" -> COLOR_PORTAL_OUTLINE;
            default -> COLOR_ERROR_OUTLINE;
        };
    }
    
    public void stop() {
        if (renderLoop != null) {
            renderLoop.stop();
        }
    }
}
