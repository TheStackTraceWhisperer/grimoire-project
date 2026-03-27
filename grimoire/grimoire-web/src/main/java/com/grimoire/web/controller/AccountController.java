package com.grimoire.web.controller;

import com.grimoire.data.Account;
import com.grimoire.web.service.AccountService;
import com.grimoire.web.service.CharacterService;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.views.View;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Controller for account management.
 */
@Controller
@Secured(SecurityRule.IS_AUTHENTICATED)
@RequiredArgsConstructor
public class AccountController {
    
    private final AccountService accountService;
    private final CharacterService characterService;
    
    @Get("/account")
    @View("account")
    public Map<String, Object> account(Authentication authentication) {
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
}
