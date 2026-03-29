package com.grimoire.web.service;

import com.grimoire.data.Account;
import com.grimoire.data.Character;
import com.grimoire.data.CharacterRepository;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for character management operations.
 */
@Singleton
@RequiredArgsConstructor
public class CharacterService {
    
    private static final int MAX_CHARACTERS_PER_ACCOUNT = 5;
    
    private final CharacterRepository characterRepository;
    
    /**
     * Creates a new character for an account.
     * @param name the character name
     * @param account the account
     * @return Optional containing the created character if successful
     */
    public Optional<Character> createCharacter(String name, Account account) {
        long characterCount = characterRepository.countByAccountId(account.getId());
        if (characterCount >= MAX_CHARACTERS_PER_ACCOUNT) {
            return Optional.empty();
        }
        
        Character character = new Character(name, account);
        return Optional.of(characterRepository.save(character));
    }
    
    /**
     * Finds all characters for an account.
     * @param accountId the account ID
     * @return list of characters
     */
    public List<Character> findByAccountId(Long accountId) {
        return characterRepository.findByAccountId(accountId);
    }
    
    /**
     * Finds a character by ID.
     * @param id the character ID
     * @return Optional containing the character if found
     */
    public Optional<Character> findById(Long id) {
        return characterRepository.findById(id);
    }
    
    /**
     * Deletes a character.
     * @param id the character ID
     * @param accountId the account ID (for authorization)
     * @return true if deletion was successful
     */
    public boolean deleteCharacter(Long id, Long accountId) {
        Optional<Character> characterOpt = characterRepository.findById(id);
        if (characterOpt.isPresent()) {
            Character character = characterOpt.get();
            if (character.getAccount().getId().equals(accountId)) {
                characterRepository.delete(character);
                return true;
            }
        }
        return false;
    }
    
    /**
     * Updates the last played time for a character.
     * @param id the character ID
     */
    public void updateLastPlayed(Long id) {
        Optional<Character> characterOpt = characterRepository.findById(id);
        characterOpt.ifPresent(character -> {
            character.setLastPlayedAt(LocalDateTime.now());
            characterRepository.save(character);
        });
    }
}
