package com.grimoire.web.controller;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.views.View;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for the home page.
 */
@Controller
public class HomeController {
    
    @Get("/")
    @View("index")
    public Map<String, Object> home() {
        return new HashMap<>();
    }
}
