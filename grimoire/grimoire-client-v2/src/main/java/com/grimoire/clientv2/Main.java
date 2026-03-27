package com.grimoire.clientv2;

import com.grimoire.clientv2.core.GameEngine;
import lombok.extern.slf4j.Slf4j;

/**
 * Main entry point for the Grimoire LWJGL client.
 */
@Slf4j
public class Main {
    
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 8443;
    
    public static void main(String[] args) {
        log.info("Starting Grimoire Client V2 (LWJGL)");
        
        // Parse command line arguments
        String host = DEFAULT_HOST;
        int port = DEFAULT_PORT;
        boolean autoConnect = false;
        
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--host", "-h" -> {
                    if (i + 1 < args.length) {
                        host = args[++i];
                    }
                }
                case "--port", "-p" -> {
                    if (i + 1 < args.length) {
                        try {
                            port = Integer.parseInt(args[++i]);
                        } catch (NumberFormatException e) {
                            log.warn("Invalid port number, using default: {}", DEFAULT_PORT);
                        }
                    }
                }
                case "--connect", "-c" -> autoConnect = true;
            }
        }
        
        GameEngine engine = null;
        
        try {
            engine = new GameEngine("Grimoire MMO - V2", 1280, 720);
            engine.init();
            
            if (autoConnect) {
                log.info("Auto-connecting to {}:{}", host, port);
                try {
                    engine.connect(host, port);
                    engine.getSceneManager().switchTo("game");
                } catch (Exception e) {
                    log.error("Failed to connect to server", e);
                }
            } else {
                // Start in game scene for testing (without connection)
                engine.getSceneManager().switchTo("game");
            }
            
            engine.run();
            
        } catch (Exception e) {
            log.error("Fatal error", e);
            System.exit(1);
        } finally {
            if (engine != null) {
                engine.cleanup();
            }
        }
        
        log.info("Grimoire Client V2 shut down");
    }
}
