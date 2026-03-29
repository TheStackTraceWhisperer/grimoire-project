package com.grimoire.web;

import io.micronaut.runtime.Micronaut;

/**
 * Micronaut application for web-based account and character management.
 */
public class WebApplication {
    
    public static void main(String[] args) {
        Micronaut.run(WebApplication.class, args);
    }
}
