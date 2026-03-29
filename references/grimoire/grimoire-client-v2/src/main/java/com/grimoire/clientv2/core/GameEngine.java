package com.grimoire.clientv2.core;

import com.grimoire.clientv2.input.InputManager;
import com.grimoire.clientv2.network.NetworkClient;
import com.grimoire.clientv2.render.Renderer;
import com.grimoire.clientv2.render.ui.UIRenderer;
import com.grimoire.clientv2.scene.GameScene;
import com.grimoire.clientv2.scene.Scene;
import com.grimoire.clientv2.scene.SceneManager;
import com.grimoire.clientv2.state.ClientEcsWorld;
import com.grimoire.shared.dto.*;
import com.grimoire.shared.protocol.GamePacket;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Main game engine class that manages the game loop and subsystems.
 */
@Slf4j
public class GameEngine {
    
    private static final int TARGET_FPS = 60;
    private static final double FRAME_TIME = 1.0 / TARGET_FPS;
    
    @Getter
    private Window window;
    
    @Getter
    private InputManager inputManager;
    
    @Getter
    private Renderer renderer;
    
    @Getter
    private UIRenderer uiRenderer;
    
    @Getter
    private SceneManager sceneManager;
    
    @Getter
    private NetworkClient networkClient;
    
    @Getter
    private ClientEcsWorld ecsWorld;
    
    // Thread-safe queue for network packet processing
    private final Queue<GamePacket> packetQueue = new ConcurrentLinkedQueue<>();
    
    private boolean running;
    private double lastTime;
    private double deltaTime;
    private int fps;
    private int frameCount;
    private double fpsTimer;
    
    /**
     * Creates a new game engine instance.
     *
     * @param title  Window title
     * @param width  Window width
     * @param height Window height
     */
    public GameEngine(String title, int width, int height) {
        window = new Window(title, width, height, true);
        inputManager = new InputManager();
        renderer = new Renderer();
        uiRenderer = new UIRenderer();
        sceneManager = new SceneManager();
        networkClient = new NetworkClient();
        ecsWorld = new ClientEcsWorld();
    }
    
    /**
     * Initializes the game engine.
     */
    public void init() throws IOException {
        log.info("Initializing game engine...");
        
        // Initialize window
        window.init();
        
        // Initialize input
        inputManager.init(window.getHandle());
        
        // Initialize rendering
        renderer.init(window);
        uiRenderer.init(window.getWidth(), window.getHeight());
        
        // Setup network packet handler
        networkClient.setPacketHandler(this::queuePacket);
        networkClient.setDisconnectHandler(this::onDisconnect);
        
        // Create and register scenes
        GameScene gameScene = new GameScene(ecsWorld, networkClient);
        gameScene.init(window, inputManager, renderer, uiRenderer);
        gameScene.setOnDisconnect(() -> {
            networkClient.disconnect();
            window.setShouldClose(true);
        });
        sceneManager.registerScene("game", gameScene);
        
        log.info("Game engine initialized");
    }
    
    /**
     * Queues a packet for processing on the main thread.
     */
    private void queuePacket(GamePacket packet) {
        packetQueue.add(packet);
    }
    
    /**
     * Processes queued network packets.
     */
    private void processPackets() {
        GamePacket packet;
        while ((packet = packetQueue.poll()) != null) {
            handlePacket(packet);
        }
    }
    
    /**
     * Handles a received packet.
     */
    private void handlePacket(GamePacket packet) {
        log.debug("Processing packet: {}", packet.type());
        
        switch (packet.type()) {
            case S2C_CHARACTER_LIST -> {
                if (packet.payload() instanceof CharacterListResponse response) {
                    log.info("Received character list: {} characters", response.characters().size());
                    // In a full implementation, would show character selection UI
                }
            }
            case S2C_CHARACTER_SELECTION_SUCCESS -> {
                if (packet.payload() instanceof CharacterSelectionSuccess success) {
                    log.info("Character selected: {}", success.characterName());
                    ecsWorld.setLocalPlayerEntityId(success.entityId());
                    ecsWorld.setCurrentZone(success.zone());
                    sceneManager.switchTo("game");
                }
            }
            case S2C_LOGIN_FAILURE -> {
                if (packet.payload() instanceof LoginFailure failure) {
                    log.warn("Login failed: {}", failure.reason());
                }
            }
            case S2C_ZONE_CHANGE -> {
                if (packet.payload() instanceof ZoneChange zoneChange) {
                    log.info("Zone change to: {}", zoneChange.newZoneId());
                    ecsWorld.clearAllEntities();
                    ecsWorld.setCurrentZone(zoneChange.newZoneId());
                }
            }
            case S2C_GAME_STATE_UPDATE -> {
                if (packet.payload() instanceof GameStateUpdate update) {
                    ecsWorld.processStateUpdate(update);
                }
            }
            case S2C_ENTITY_SPAWN -> {
                if (packet.payload() instanceof EntitySpawn spawn) {
                    ecsWorld.spawnEntity(spawn);
                }
            }
            case S2C_ENTITY_DESPAWN -> {
                if (packet.payload() instanceof EntityDespawn despawn) {
                    ecsWorld.despawnEntity(despawn);
                }
            }
            case S2C_CHAT_BROADCAST -> {
                if (packet.payload() instanceof ChatBroadcast chat) {
                    log.info("Chat: {}: {}", chat.sender(), chat.message());
                }
            }
            default -> log.debug("Unhandled packet type: {}", packet.type());
        }
    }
    
    /**
     * Called when disconnected from server.
     */
    private void onDisconnect() {
        log.info("Disconnected from server");
    }
    
    /**
     * Connects to the game server.
     */
    public void connect(String host, int port) throws Exception {
        networkClient.connect(host, port);
    }
    
    /**
     * Starts the game loop.
     */
    public void run() {
        running = true;
        lastTime = getTime();
        
        log.info("Starting game loop");
        
        while (running && !window.shouldClose()) {
            double currentTime = getTime();
            deltaTime = currentTime - lastTime;
            lastTime = currentTime;
            
            // Process network packets
            processPackets();
            
            // Handle window resize
            if (window.isResized()) {
                int width = window.getWidth();
                int height = window.getHeight();
                renderer.resize(width, height);
                uiRenderer.resize(width, height);
                sceneManager.onResize(width, height);
                window.clearResizedFlag();
            }
            
            // Update
            sceneManager.update((float) deltaTime);
            
            // Render
            sceneManager.render();
            
            // Update window
            window.update();
            
            // Update input state
            inputManager.update();
            
            // FPS counter
            frameCount++;
            fpsTimer += deltaTime;
            if (fpsTimer >= 1.0) {
                fps = frameCount;
                frameCount = 0;
                fpsTimer = 0;
                log.trace("FPS: {}", fps);
            }
            
            // Frame rate limiting
            double frameEnd = getTime();
            double frameTimeTaken = frameEnd - currentTime;
            if (frameTimeTaken < FRAME_TIME) {
                try {
                    Thread.sleep((long) ((FRAME_TIME - frameTimeTaken) * 1000));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
    
    /**
     * Gets the current time in seconds.
     */
    private double getTime() {
        return System.nanoTime() / 1_000_000_000.0;
    }
    
    /**
     * Gets the current FPS.
     */
    public int getFps() {
        return fps;
    }
    
    /**
     * Stops the game loop.
     */
    public void stop() {
        running = false;
    }
    
    /**
     * Cleans up all resources.
     */
    public void cleanup() {
        log.info("Cleaning up game engine...");
        
        networkClient.disconnect();
        sceneManager.cleanup();
        uiRenderer.cleanup();
        renderer.cleanup();
        inputManager.cleanup();
        window.cleanup();
        
        log.info("Game engine cleanup complete");
    }
}
