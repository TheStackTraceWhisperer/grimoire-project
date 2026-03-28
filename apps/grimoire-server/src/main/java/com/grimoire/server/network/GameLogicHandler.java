package com.grimoire.server.network;

import com.grimoire.data.Account;
import com.grimoire.data.Character;
import com.grimoire.data.CharacterRepository;
import com.grimoire.server.component.*;
import com.grimoire.ecs.EcsWorld;
import com.grimoire.ecs.GameCommandQueue;
import com.grimoire.server.auth.AuthenticationService;
import com.grimoire.server.security.TokenValidationService;
import com.grimoire.server.session.Session;
import com.grimoire.server.session.SessionManager;
import com.grimoire.server.system.SpatialGridSystem;
import com.grimoire.shared.dto.*;
import com.grimoire.shared.protocol.GamePacket;
import com.grimoire.shared.protocol.PacketType;
import io.micronaut.context.annotation.Prototype;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Handles game packet processing per connection with OAuth2 authentication and character selection.
 * 
 * <p>All ECS modifications are enqueued via {@link GameCommandQueue} to ensure
 * thread-safe execution on the main game loop thread.</p>
 */
@Prototype
@RequiredArgsConstructor
@Slf4j
public class GameLogicHandler extends SimpleChannelInboundHandler<GamePacket> {
    
    private final EcsWorld ecsWorld;
    private final GameCommandQueue commandQueue;
    private final AuthenticationService authService;
    private final CharacterRepository characterRepository;
    private final TokenValidationService tokenValidationService;
    private final SessionManager sessionManager;
    private final com.grimoire.server.service.GroupService groupService;
    private final SpatialGridSystem spatialGridSystem;
    private volatile String entityId;
    private String sessionId;
    private Long accountId;
    
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GamePacket packet) {
        switch (packet.type()) {
            case C2S_TOKEN_LOGIN_REQUEST:
                handleTokenLoginRequest(ctx, packet);
                break;
            case C2S_REQUEST_CHARACTER_LIST:
                handleRequestCharacterList(ctx);
                break;
            case C2S_CHARACTER_SELECTION:
                handleCharacterSelection(ctx, packet);
                break;
            case C2S_MOVEMENT_INTENT:
                handleMovementIntent(ctx, packet);
                break;
            case C2S_CHAT_MESSAGE:
                handleChatMessage(ctx, packet);
                break;
            case C2S_PRIVATE_MESSAGE:
                handlePrivateMessage(ctx, packet);
                break;
            case C2S_CREATE_GROUP:
                handleCreateGroup(ctx, packet);
                break;
            case C2S_GROUP_MESSAGE:
                handleGroupMessage(ctx, packet);
                break;
            case C2S_JOIN_GROUP:
                handleJoinGroup(ctx, packet);
                break;
            case C2S_LEAVE_GROUP:
                handleLeaveGroup(ctx, packet);
                break;
            default:
                log.warn("Unknown packet type: {}", packet.type());
        }
    }
    
    private void handleTokenLoginRequest(ChannelHandlerContext ctx, GamePacket packet) {
        if (!(packet.payload() instanceof TokenLoginRequest req)) {
            return;
        }
        
        Optional<String> usernameOpt = tokenValidationService.validateTokenAndGetUsername(req.accessToken());
        
        if (usernameOpt.isPresent()) {
            String username = usernameOpt.get();
            
            // Get or create account
            Optional<Account> accountOpt = authService.createOAuthAccount(username);
            
            if (accountOpt.isPresent()) {
                Account account = accountOpt.get();
                this.accountId = account.getId();
                
                // Create session
                Session session = sessionManager.createSession(account.getId(), account.getUsername());
                this.sessionId = session.sessionId();
                
                // Load characters for this account
                List<Character> characters = characterRepository.findByAccountId(account.getId());
                
                // Convert to DTO
                List<CharacterListResponse.CharacterInfo> characterInfos = characters.stream()
                        .map(c -> new CharacterListResponse.CharacterInfo(
                                c.getId(),
                                c.getName(),
                                c.getLevel(),
                                c.getLastZone()
                        ))
                        .collect(Collectors.toList());
                
                // Send character list to client
                CharacterListResponse response = new CharacterListResponse(session.sessionId(), characterInfos);
                ctx.writeAndFlush(new GamePacket(PacketType.S2C_CHARACTER_LIST, response));
                
                log.info("User {} authenticated via OAuth2, {} characters available", username, characters.size());
            } else {
                LoginFailure failure = new LoginFailure("Failed to create account");
                ctx.writeAndFlush(new GamePacket(PacketType.S2C_LOGIN_FAILURE, failure));
                log.warn("Failed to create OAuth2 account for user {}", username);
            }
        } else {
            LoginFailure failure = new LoginFailure("Invalid token");
            ctx.writeAndFlush(new GamePacket(PacketType.S2C_LOGIN_FAILURE, failure));
            log.warn("Token validation failed");
        }
    }
    
    private void handleRequestCharacterList(ChannelHandlerContext ctx) {
        if (this.accountId == null) {
            // Not authenticated, ignore
            return;
        }
        
        List<Character> characters = characterRepository.findByAccountId(this.accountId);
        List<CharacterListResponse.CharacterInfo> characterInfos = characters.stream()
                .map(c -> new CharacterListResponse.CharacterInfo(
                        c.getId(),
                        c.getName(),
                        c.getLevel(),
                        c.getLastZone()
                ))
                .collect(Collectors.toList());

        CharacterListResponse response = new CharacterListResponse(this.sessionId, characterInfos);
        ctx.writeAndFlush(new GamePacket(PacketType.S2C_CHARACTER_LIST, response));
    }
    
    private void handleCharacterSelection(ChannelHandlerContext ctx, GamePacket packet) {
        if (!(packet.payload() instanceof CharacterSelectionRequest req)) {
            return;
        }
        
        if (accountId == null) {
            LoginFailure failure = new LoginFailure("Not authenticated");
            ctx.writeAndFlush(new GamePacket(PacketType.S2C_LOGIN_FAILURE, failure));
            return;
        }
        
        Optional<Character> characterOpt = characterRepository.findById(req.characterId());
        
        if (characterOpt.isEmpty() || !characterOpt.get().getAccount().getId().equals(accountId)) {
            LoginFailure failure = new LoginFailure("Invalid character selection");
            ctx.writeAndFlush(new GamePacket(PacketType.S2C_LOGIN_FAILURE, failure));
            log.warn("Invalid character selection: id={}, accountId={}", req.characterId(), accountId);
            return;
        }
        
        Character character = characterOpt.get();
        
        // Update last played time
        character.setLastPlayedAt(LocalDateTime.now());
        characterRepository.update(character);
        
        // Create entity ID synchronously (ID generation is thread-safe)
        // but enqueue component additions to game loop thread
        String newEntityId = ecsWorld.createEntity();
        this.entityId = newEntityId;
        
        // Capture values for lambda
        String charName = character.getName();
        String lastZone = character.getLastZone();
        double lastX = character.getLastX();
        double lastY = character.getLastY();
        long charId = character.getId();
        int currentHp = character.getCurrentHp();
        int maxHp = character.getMaxHp();
        int currentXp = character.getCurrentXp();
        int xpToNextLevel = character.getXpToNextLevel();
        var channel = ctx.channel();
        
        commandQueue.enqueue(() -> {
            ecsWorld.addComponent(newEntityId, new Zone(lastZone));
            ecsWorld.addComponent(newEntityId, new Position(lastX, lastY));
            ecsWorld.addComponent(newEntityId, new Velocity(0, 0));
            ecsWorld.addComponent(newEntityId, new Renderable(charName, "visual-player"));
            ecsWorld.addComponent(newEntityId, new Stats(currentHp, maxHp, 10, 10));
            ecsWorld.addComponent(newEntityId, new PlayerConnection(channel));
            ecsWorld.addComponent(newEntityId, new Persistent(String.valueOf(charId)));
            ecsWorld.addComponent(newEntityId, new BoundingBox(10, 10));
            ecsWorld.addComponent(newEntityId, new Solid());  // Players are solid for spatial grid tracking
            ecsWorld.addComponent(newEntityId, new Experience(currentXp, xpToNextLevel));
            // Mark as dirty so NetworkSyncSystem broadcasts ENTITY_SPAWN to other players
            ecsWorld.addComponent(newEntityId, new Dirty(ecsWorld.getCurrentTick()));
        });
        
        // Send success response
        CharacterSelectionSuccess success = new CharacterSelectionSuccess(
                newEntityId,
                character.getName(),
                character.getLevel(),
                character.getLastZone(),
                character.getLastX(),
                character.getLastY()
        );
        ctx.writeAndFlush(new GamePacket(PacketType.S2C_CHARACTER_SELECTION_SUCCESS, success));
        
        log.info("Character {} (level {}) selected for account {}, entity {}", 
                character.getName(), character.getLevel(), accountId, newEntityId);
    }
    
    private void handleMovementIntent(ChannelHandlerContext ctx, GamePacket packet) {
        if (entityId == null || !(packet.payload() instanceof com.grimoire.shared.dto.MovementIntent intent)) {
            return;
        }
        
        // Capture entity ID for lambda
        String currentEntityId = this.entityId;
        double targetX = intent.targetX();
        double targetY = intent.targetY();
        
        commandQueue.enqueue(() -> {
            ecsWorld.addComponent(currentEntityId, new com.grimoire.server.component.MovementIntent(
                    targetX, targetY));
        });
    }
    
    private void handleChatMessage(ChannelHandlerContext ctx, GamePacket packet) {
        if (entityId == null || !(packet.payload() instanceof ChatMessage msg)) {
            return;
        }
        
        // Get player name
        ecsWorld.getComponent(entityId, Renderable.class).ifPresent(renderable -> {
            ChatBroadcast broadcast = new ChatBroadcast(renderable.name(), msg.message());
            GamePacket broadcastPacket = new GamePacket(PacketType.S2C_CHAT_BROADCAST, broadcast);
            
            // Broadcast to all players
            for (String playerId : ecsWorld.getEntitiesWithComponent(PlayerConnection.class)) {
                ecsWorld.getComponent(playerId, PlayerConnection.class).ifPresent(conn ->
                        conn.channel().writeAndFlush(broadcastPacket));
            }
        });
    }
    
    private void handlePrivateMessage(ChannelHandlerContext ctx, GamePacket packet) {
        if (entityId == null || !(packet.payload() instanceof PrivateMessage msg)) {
            return;
        }
        
        // Get sender name
        ecsWorld.getComponent(entityId, Renderable.class).ifPresent(senderRenderable -> {
            String senderName = senderRenderable.name();
            String recipientName = msg.recipientName();
            
            // Find recipient by name
            boolean found = false;
            for (String playerId : ecsWorld.getEntitiesWithComponent(PlayerConnection.class)) {
                Optional<Renderable> recipientRenderable = ecsWorld.getComponent(playerId, Renderable.class);
                if (recipientRenderable.isPresent() && recipientRenderable.get().name().equals(recipientName)) {
                    ecsWorld.getComponent(playerId, PlayerConnection.class).ifPresent(conn -> {
                        PrivateMessageBroadcast broadcast = new PrivateMessageBroadcast(senderName, msg.message());
                        conn.channel().writeAndFlush(new GamePacket(PacketType.S2C_PRIVATE_MESSAGE_BROADCAST, broadcast));
                    });
                    found = true;
                    break;
                }
            }
            
            if (!found) {
                log.warn("Recipient {} not found for private message from {}", recipientName, senderName);
            }
        });
    }
    
    private void handleCreateGroup(ChannelHandlerContext ctx, GamePacket packet) {
        if (accountId == null || !(packet.payload() instanceof CreateGroup req)) {
            return;
        }
        
        Optional<com.grimoire.data.PlayerGroup> groupOpt = groupService.createGroup(req.groupName(), accountId);
        
        if (groupOpt.isPresent()) {
            CreateGroupResponse response = new CreateGroupResponse(true, "Group created successfully", groupOpt.get().getId());
            ctx.writeAndFlush(new GamePacket(PacketType.S2C_CREATE_GROUP_RESPONSE, response));
            log.info("Group {} created by account {}", req.groupName(), accountId);
        } else {
            CreateGroupResponse response = new CreateGroupResponse(false, "Failed to create group (name may already exist)", null);
            ctx.writeAndFlush(new GamePacket(PacketType.S2C_CREATE_GROUP_RESPONSE, response));
        }
    }
    
    private void handleGroupMessage(ChannelHandlerContext ctx, GamePacket packet) {
        if (entityId == null || accountId == null || !(packet.payload() instanceof GroupMessage msg)) {
            return;
        }
        
        // Check if sender is a member of the group
        if (!groupService.isMemberOfGroup(msg.groupName(), accountId)) {
            log.warn("Account {} attempted to send message to group {} but is not a member", accountId, msg.groupName());
            return;
        }
        
        // Get sender name
        ecsWorld.getComponent(entityId, Renderable.class).ifPresent(senderRenderable -> {
            String senderName = senderRenderable.name();
            GroupMessageBroadcast broadcast = new GroupMessageBroadcast(msg.groupName(), senderName, msg.message());
            GamePacket broadcastPacket = new GamePacket(PacketType.S2C_GROUP_MESSAGE_BROADCAST, broadcast);
            
            // Get all group members
            List<com.grimoire.data.Account> members = groupService.getGroupMembers(msg.groupName());
            
            // Broadcast to all online members
            for (String playerId : ecsWorld.getEntitiesWithComponent(PlayerConnection.class)) {
                ecsWorld.getComponent(playerId, Persistent.class).ifPresent(persistent -> {
                    try {
                        Long characterId = Long.parseLong(persistent.accountId());
                        characterRepository.findById(characterId).ifPresent(character -> {
                            Long playerAccountId = character.getAccount().getId();
                            if (members.stream().anyMatch(member -> member.getId().equals(playerAccountId))) {
                                ecsWorld.getComponent(playerId, PlayerConnection.class).ifPresent(conn ->
                                        conn.channel().writeAndFlush(broadcastPacket));
                            }
                        });
                    } catch (NumberFormatException e) {
                        log.warn("Invalid character ID in persistent component: {}", persistent.accountId());
                    }
                });
            }
        });
    }
    
    private void handleJoinGroup(ChannelHandlerContext ctx, GamePacket packet) {
        if (accountId == null || !(packet.payload() instanceof JoinGroup req)) {
            return;
        }
        
        boolean success = groupService.joinGroup(req.groupName(), accountId);
        
        if (success) {
            JoinGroupResponse response = new JoinGroupResponse(true, "Joined group successfully");
            ctx.writeAndFlush(new GamePacket(PacketType.S2C_JOIN_GROUP_RESPONSE, response));
            log.info("Account {} joined group {}", accountId, req.groupName());
        } else {
            JoinGroupResponse response = new JoinGroupResponse(false, "Failed to join group (may not exist or already a member)");
            ctx.writeAndFlush(new GamePacket(PacketType.S2C_JOIN_GROUP_RESPONSE, response));
        }
    }
    
    private void handleLeaveGroup(ChannelHandlerContext ctx, GamePacket packet) {
        if (accountId == null || !(packet.payload() instanceof LeaveGroup req)) {
            return;
        }
        
        boolean success = groupService.leaveGroup(req.groupName(), accountId);
        
        if (success) {
            LeaveGroupResponse response = new LeaveGroupResponse(true, "Left group successfully");
            ctx.writeAndFlush(new GamePacket(PacketType.S2C_LEAVE_GROUP_RESPONSE, response));
            log.info("Account {} left group {}", accountId, req.groupName());
        } else {
            LeaveGroupResponse response = new LeaveGroupResponse(false, "Failed to leave group (may not exist or not a member)");
            ctx.writeAndFlush(new GamePacket(PacketType.S2C_LEAVE_GROUP_RESPONSE, response));
        }
    }
    
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if (sessionId != null) {
            sessionManager.invalidateSession(sessionId);
            log.info("Session {} invalidated on disconnect", sessionId);
        }
        
        if (entityId != null) {
            String entityToDestroy = entityId;
            log.info("Player disconnected, scheduling entity {} for destruction", entityToDestroy);
            entityId = null;
            
            // Save player state and destroy entity on the game loop thread
            commandQueue.enqueue(() -> {
                savePlayerState(entityToDestroy);
                // Remove from spatial grid before destroying to prevent ghost collisions
                spatialGridSystem.removeEntity(entityToDestroy);
                ecsWorld.destroyEntity(entityToDestroy);
            });
        }
    }
    
    /**
     * Saves the player's current state (position, HP, XP) to the database.
     */
    private void savePlayerState(String playerEntityId) {
        Optional<Persistent> persistentOpt = ecsWorld.getComponent(playerEntityId, Persistent.class);
        if (persistentOpt.isEmpty()) {
            return;
        }
        
        try {
            Long characterId = Long.parseLong(persistentOpt.get().accountId());
            Optional<Character> characterOpt = characterRepository.findById(characterId);
            
            if (characterOpt.isEmpty()) {
                log.warn("Character {} not found during save", characterId);
                return;
            }
            
            Character character = characterOpt.get();
            
            // Save position
            ecsWorld.getComponent(playerEntityId, Position.class).ifPresent(pos -> {
                character.setLastX(pos.x());
                character.setLastY(pos.y());
            });
            
            // Save zone
            ecsWorld.getComponent(playerEntityId, Zone.class).ifPresent(zone -> 
                character.setLastZone(zone.zoneId()));
            
            // Save HP
            ecsWorld.getComponent(playerEntityId, Stats.class).ifPresent(stats -> {
                character.setCurrentHp(stats.hp());
                character.setMaxHp(stats.maxHp());
            });
            
            // Save Experience
            ecsWorld.getComponent(playerEntityId, Experience.class).ifPresent(exp -> {
                character.setCurrentXp(exp.currentXp());
                character.setXpToNextLevel(exp.xpToNextLevel());
            });
            
            character.setLastPlayedAt(LocalDateTime.now());
            characterRepository.update(character);
            
            log.info("Saved player state for character {}", characterId);
        } catch (NumberFormatException e) {
            log.warn("Invalid character ID in persistent component: {}", persistentOpt.get().accountId());
        }
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Exception in game handler", cause);
        ctx.close();
    }
}
