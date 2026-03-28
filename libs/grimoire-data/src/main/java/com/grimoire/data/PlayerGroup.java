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
 * PlayerGroup entity representing a persistent group of players.
 * Groups persist through logout and allow community formation.
 */
@Entity
@Table(name = "player_groups")
@Getter
@Setter
@NoArgsConstructor
@SuppressWarnings("PMD.CommentRequired")
@SuppressFBWarnings(
        value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"},
        justification = "JPA entity relationships are mutable references managed by the persistence context."
)
public class PlayerGroup {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 50)
    private String name;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_account_id", nullable = false)
    private Account owner;
    
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GroupMembership> memberships = new ArrayList<>();
    
    public PlayerGroup(String name, Account owner) {
        this.name = name;
        this.owner = owner;
    }
}
