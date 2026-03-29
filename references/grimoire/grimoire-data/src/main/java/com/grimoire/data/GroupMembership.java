package com.grimoire.data;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * GroupMembership entity representing a player's membership in a group.
 * This entity tracks which accounts belong to which groups.
 */
@Entity
@Table(name = "group_memberships",
       uniqueConstraints = @UniqueConstraint(columnNames = {"group_id", "account_id"}))
@Getter
@Setter
@NoArgsConstructor
public class GroupMembership {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private PlayerGroup group;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;
    
    @Column(nullable = false)
    private LocalDateTime joinedAt = LocalDateTime.now();
    
    public GroupMembership(PlayerGroup group, Account account) {
        this.group = group;
        this.account = account;
        this.joinedAt = LocalDateTime.now();
    }
}
