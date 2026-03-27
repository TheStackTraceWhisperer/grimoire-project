package com.grimoire.web.controller;

import com.grimoire.data.Account;
import com.grimoire.data.Character;
import com.grimoire.web.service.AccountService;
import com.grimoire.web.service.CharacterService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.views.View;
import lombok.RequiredArgsConstructor;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Controller for character management with HTMX support.
 */
@Controller
@Secured(SecurityRule.IS_AUTHENTICATED)
@RequiredArgsConstructor
public class CharacterController {
    
    private final AccountService accountService;
    private final CharacterService characterService;
    
    @Get("/characters")
    @View("characters")
    public Map<String, Object> characters(Authentication authentication) {
        String username = authentication.getName();
        Optional<Account> accountOpt = accountService.findByUsername(username);
        
        Map<String, Object> model = new HashMap<>();
        if (accountOpt.isEmpty()) {
            return model;
        }
        
        Account account = accountOpt.get();
        model.put("account", account);
        model.put("characters", characterService.findByAccountId(account.getId()));
        
        return model;
    }
    
    @Post("/characters/create")
    public HttpResponse<?> createCharacter(
            Authentication authentication,
            @QueryValue String name) {
        
        String username = authentication.getName();
        Optional<Account> accountOpt = accountService.findByUsername(username);
        
        if (accountOpt.isEmpty()) {
            return HttpResponse.seeOther(URI.create("/login"));
        }
        
        Account account = accountOpt.get();
        
        // Validate character name
        if (name == null || name.trim().isEmpty()) {
            return HttpResponse.seeOther(URI.create("/characters?error=" + "Character+name+is+required"));
        }
        
        if (name.length() < 3 || name.length() > 20) {
            return HttpResponse.seeOther(URI.create("/characters?error=" + "Character+name+must+be+between+3+and+20+characters"));
        }
        
        // Attempt to create character
        Optional<Character> character = characterService.createCharacter(name, account);
        if (character.isEmpty()) {
            return HttpResponse.seeOther(URI.create("/characters?error=" + "Maximum+of+5+characters+per+account+reached"));
        }
        
        return HttpResponse.seeOther(URI.create("/characters?success=" + "Character+created+successfully"));
    }
    
    @Post("/characters/delete")
    public HttpResponse<?> deleteCharacter(
            Authentication authentication,
            @QueryValue Long characterId) {
        
        String username = authentication.getName();
        Optional<Account> accountOpt = accountService.findByUsername(username);
        
        if (accountOpt.isEmpty()) {
            return HttpResponse.seeOther(URI.create("/login"));
        }
        
        Account account = accountOpt.get();
        boolean success = characterService.deleteCharacter(characterId, account.getId());
        
        if (!success) {
            return HttpResponse.seeOther(URI.create("/characters?error=" + "Failed+to+delete+character"));
        }
        
        return HttpResponse.seeOther(URI.create("/characters?success=" + "Character+deleted+successfully"));
    }
}
