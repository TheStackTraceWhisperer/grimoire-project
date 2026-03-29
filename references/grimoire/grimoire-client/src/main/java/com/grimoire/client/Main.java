package com.grimoire.client;

import io.micronaut.context.ApplicationContext;

/**
 * Main entry point for the Grimoire client application.
 */
public class Main {
    
    public static void main(String[] args) {
        ApplicationContext context = ApplicationContext.run();
        JavaFxApplication app = context.getBean(JavaFxApplication.class);
        app.launchApp(args);
    }
}
