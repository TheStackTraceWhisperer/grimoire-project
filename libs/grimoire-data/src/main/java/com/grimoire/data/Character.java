package com.grimoire.data;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Character entity representing a player's game character.
 */
@Entity
@Table(name = "characters")
@Getter
@Setter
@NoArgsConstructor
@SuppressWarnings("PMD.CommentRequired")
@SuppressFBWarnings(
        value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"},
        justification = "JPA entity relationships are mutable references managed by the persistence context."
)
public class Character {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 50)
    private String name;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;
    
    @Column(nullable = false)
    private int level = 1;
    
    @Column(nullable = false)
    private double lastX = 100.0;
    
    @Column(nullable = false)
    private double lastY = 100.0;
    
    @Column(nullable = false, length = 50)
    private String lastZone = "zone1";
    
    @Column(nullable = false)
    private int currentHp = 100;
    
    @Column(nullable = false)
    private int maxHp = 100;
    
    @Column(nullable = false)
    private int currentXp;
    
    @Column(nullable = false)
    private int xpToNextLevel = 100;
    
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(nullable = false)
    private LocalDateTime lastPlayedAt = LocalDateTime.now();
    
    public Character(String name, Account account) {
        this.name = name;
        this.account = account;
    }
}
