package com.grimoire.server;

import io.micronaut.runtime.Micronaut;

/**
 * Main application entry point.
 */
public class Application {
    
    public static void main(String[] args) {
        Micronaut.run(Application.class, args);
    }
}
