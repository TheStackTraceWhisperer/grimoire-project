package com.grimoire.data;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Account entity for OAuth2 authenticated players.
 * Authentication is handled entirely by Keycloak/OAuth2.
 */
@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
@SuppressWarnings("PMD.CommentRequired")
@SuppressFBWarnings(
        value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"},
        justification = "JPA entity relationships are mutable references managed by the persistence context."
)
public class Account {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 100)
    private String username;
    
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Character> characters = new ArrayList<>();
    
    public Account(String username) {
        this.username = username;
    }
}
