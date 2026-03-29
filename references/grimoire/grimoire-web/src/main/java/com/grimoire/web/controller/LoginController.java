package com.grimoire.web.controller;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.views.View;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Controller for login page.
 */
@Controller
public class LoginController {
    
    @Get("/login")
    @View("login")
    public Map<String, Object> login(
            @QueryValue Optional<String> error,
            @QueryValue Optional<String> logout) {
        
        Map<String, Object> model = new HashMap<>();
        
        if (error.isPresent()) {
            model.put("error", "Invalid username or password");
        }
        
        if (logout.isPresent()) {
            model.put("success", "You have been logged out successfully");
        }
        
        return model;
    }
}
