package com.grimoire.server.network;

import com.grimoire.data.Account;
import com.grimoire.data.AccountRepository;
import com.grimoire.data.Character;
import com.grimoire.data.CharacterRepository;
import com.grimoire.server.auth.AuthenticationService;
import com.grimoire.server.component.*;
import com.grimoire.server.ecs.ComponentManager;
import com.grimoire.server.ecs.EcsWorld;
import com.grimoire.server.ecs.EntityManager;
import com.grimoire.server.ecs.GameCommandQueue;
import com.grimoire.server.security.TokenValidationService;
import com.grimoire.server.session.Session;
import com.grimoire.server.session.SessionManager;
import com.grimoire.shared.dto.*;
import com.grimoire.shared.protocol.GamePacket;
import com.grimoire.shared.protocol.PacketType;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Integration tests for GameLogicHandler with mocked dependencies.
 */
class GameLogicHandlerIntegrationTest {
    
    private EcsWorld ecsWorld;
    private GameCommandQueue commandQueue;
    private AuthenticationService authService;
    private CharacterRepository characterRepository;
    private TokenValidationService tokenValidationService;
    private SessionManager sessionManager;
    private AccountRepository accountRepository;
    private GameLogicHandler handler;
    private ChannelHandlerContext ctx;
    private Channel channel;
    
    @BeforeEach
    void setUp() {
        EntityManager entityManager = new EntityManager();
        ComponentManager componentManager = new ComponentManager();
        ecsWorld = new EcsWorld(entityManager, componentManager);
        commandQueue = new GameCommandQueue();
        
        com.grimoire.server.config.GameConfig gameConfig = com.grimoire.server.config.TestGameConfig.create();
        accountRepository = Mockito.mock(AccountRepository.class);
        authService = new AuthenticationService(accountRepository);
        characterRepository = Mockito.mock(CharacterRepository.class);
        tokenValidationService = Mockito.mock(TokenValidationService.class);
        sessionManager = new SessionManager(gameConfig);
        com.grimoire.server.service.GroupService groupService = Mockito.mock(com.grimoire.server.service.GroupService.class);
        com.grimoire.server.system.SpatialGridSystem spatialGridSystem = new com.grimoire.server.system.SpatialGridSystem(ecsWorld, gameConfig);
        
        handler = new GameLogicHandler(ecsWorld, commandQueue, authService, characterRepository, 
                                       tokenValidationService, sessionManager, groupService, spatialGridSystem);
        
        ctx = Mockito.mock(ChannelHandlerContext.class);
        channel = Mockito.mock(Channel.class);
        when(ctx.channel()).thenReturn(channel);
        when(ctx.writeAndFlush(any())).thenReturn(null);
        when(channel.writeAndFlush(any())).thenReturn(null);
    }
    
    /**
     * Helper method to drain the command queue, simulating the game loop tick.
     */
    private void drainCommandQueue() {
        commandQueue.drain();
    }
    
    @Test
    void testTokenLoginRequestSuccess() throws Exception {
        // Setup mocks
        String token = "valid-token";
        String username = "testuser";
        Account mockAccount = Mockito.mock(Account.class);
        
        when(tokenValidationService.validateTokenAndGetUsername(token))
            .thenReturn(Optional.of(username));
        when(accountRepository.existsByUsername(username)).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenReturn(mockAccount);
        when(mockAccount.getId()).thenReturn(1L);
        when(mockAccount.getUsername()).thenReturn(username);
        when(characterRepository.findByAccountId(1L)).thenReturn(new ArrayList<>());
        
        // Create token login request
        TokenLoginRequest request = new TokenLoginRequest(token);
        GamePacket packet = new GamePacket(PacketType.C2S_TOKEN_LOGIN_REQUEST, request);
        
        // Process the packet
        handler.channelRead0(ctx, packet);
        
        // Verify character list response was sent
        ArgumentCaptor<GamePacket> packetCaptor = ArgumentCaptor.forClass(GamePacket.class);
        verify(ctx).writeAndFlush(packetCaptor.capture());
        
        GamePacket sentPacket = packetCaptor.getValue();
        assertEquals(PacketType.S2C_CHARACTER_LIST, sentPacket.type());
        assertInstanceOf(CharacterListResponse.class, sentPacket.payload());
    }
    
    @Test
    void testTokenLoginRequestInvalidToken() throws Exception {
        // Setup mocks
        String token = "invalid-token";
        
        when(tokenValidationService.validateTokenAndGetUsername(token))
            .thenReturn(Optional.empty());
        
        // Create token login request
        TokenLoginRequest request = new TokenLoginRequest(token);
        GamePacket packet = new GamePacket(PacketType.C2S_TOKEN_LOGIN_REQUEST, request);
        
        // Process the packet
        handler.channelRead0(ctx, packet);
        
        // Verify login failure was sent
        ArgumentCaptor<GamePacket> packetCaptor = ArgumentCaptor.forClass(GamePacket.class);
        verify(ctx).writeAndFlush(packetCaptor.capture());
        
        GamePacket sentPacket = packetCaptor.getValue();
        assertEquals(PacketType.S2C_LOGIN_FAILURE, sentPacket.type());
        assertInstanceOf(LoginFailure.class, sentPacket.payload());
        LoginFailure failure = (LoginFailure) sentPacket.payload();
        assertEquals("Invalid token", failure.reason());
    }
    
    @Test
    void testTokenLoginRequestWithExistingCharacters() throws Exception {
        // Setup mocks
        String token = "valid-token";
        String username = "testuser";
        Account mockAccount = Mockito.mock(Account.class);
        Character char1 = Mockito.mock(Character.class);
        Character char2 = Mockito.mock(Character.class);
        
        when(tokenValidationService.validateTokenAndGetUsername(token))
            .thenReturn(Optional.of(username));
        when(accountRepository.existsByUsername(username)).thenReturn(true);
        when(accountRepository.findByUsername(username)).thenReturn(Optional.of(mockAccount));
        when(mockAccount.getId()).thenReturn(1L);
        when(mockAccount.getUsername()).thenReturn(username);
        
        when(char1.getId()).thenReturn(1L);
        when(char1.getName()).thenReturn("Hero1");
        when(char1.getLevel()).thenReturn(10);
        when(char1.getLastZone()).thenReturn("zone1");
        
        when(char2.getId()).thenReturn(2L);
        when(char2.getName()).thenReturn("Hero2");
        when(char2.getLevel()).thenReturn(5);
        when(char2.getLastZone()).thenReturn("zone2");
        
        List<Character> characters = List.of(char1, char2);
        when(characterRepository.findByAccountId(1L)).thenReturn(characters);
        
        // Create token login request
        TokenLoginRequest request = new TokenLoginRequest(token);
        GamePacket packet = new GamePacket(PacketType.C2S_TOKEN_LOGIN_REQUEST, request);
        
        // Process the packet
        handler.channelRead0(ctx, packet);
        
        // Verify character list response was sent with 2 characters
        ArgumentCaptor<GamePacket> packetCaptor = ArgumentCaptor.forClass(GamePacket.class);
        verify(ctx).writeAndFlush(packetCaptor.capture());
        
        GamePacket sentPacket = packetCaptor.getValue();
        assertEquals(PacketType.S2C_CHARACTER_LIST, sentPacket.type());
        CharacterListResponse response = (CharacterListResponse) sentPacket.payload();
        assertEquals(2, response.characters().size());
        assertEquals("Hero1", response.characters().get(0).name());
        assertEquals("Hero2", response.characters().get(1).name());
    }
    
    @Test
    void testRequestCharacterListWithoutAuthentication() throws Exception {
        // Try to request character list without logging in first
        GamePacket packet = new GamePacket(PacketType.C2S_REQUEST_CHARACTER_LIST, null);
        
        // Process the packet
        handler.channelRead0(ctx, packet);
        
        // Should not send any response (not authenticated)
        verify(ctx, never()).writeAndFlush(any());
    }
    
    @Test
    void testCharacterSelectionSuccess() throws Exception {
        // First, authenticate
        String token = "valid-token";
        String username = "testuser";
        Account mockAccount = Mockito.mock(Account.class);
        Character mockChar = Mockito.mock(Character.class);
        
        when(tokenValidationService.validateTokenAndGetUsername(token))
            .thenReturn(Optional.of(username));
        when(accountRepository.existsByUsername(username)).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenReturn(mockAccount);
        when(mockAccount.getId()).thenReturn(1L);
        when(mockAccount.getUsername()).thenReturn(username);
        when(characterRepository.findByAccountId(1L)).thenReturn(new ArrayList<>());
        
        TokenLoginRequest loginRequest = new TokenLoginRequest(token);
        GamePacket loginPacket = new GamePacket(PacketType.C2S_TOKEN_LOGIN_REQUEST, loginRequest);
        handler.channelRead0(ctx, loginPacket);
        
        // Setup character
        when(mockChar.getId()).thenReturn(1L);
        when(mockChar.getName()).thenReturn("Hero");
        when(mockChar.getLevel()).thenReturn(10);
        when(mockChar.getLastZone()).thenReturn("zone1");
        when(mockChar.getLastX()).thenReturn(100.0);
        when(mockChar.getLastY()).thenReturn(200.0);
        when(mockChar.getAccount()).thenReturn(mockAccount);
        when(mockChar.getLastPlayedAt()).thenReturn(LocalDateTime.now());
        
        when(characterRepository.findById(1L)).thenReturn(Optional.of(mockChar));
        when(characterRepository.update(any(Character.class))).thenReturn(mockChar);
        
        // Select character
        CharacterSelectionRequest selectionRequest = new CharacterSelectionRequest(1L);
        GamePacket selectionPacket = new GamePacket(PacketType.C2S_CHARACTER_SELECTION, selectionRequest);
        handler.channelRead0(ctx, selectionPacket);
        
        // Drain the command queue to apply ECS changes
        drainCommandQueue();
        
        // Verify success response and entity creation
        ArgumentCaptor<GamePacket> packetCaptor = ArgumentCaptor.forClass(GamePacket.class);
        verify(ctx, atLeast(2)).writeAndFlush(packetCaptor.capture());
        
        // Find the character selection success packet
        List<GamePacket> sentPackets = packetCaptor.getAllValues();
        GamePacket successPacket = sentPackets.stream()
            .filter(p -> p.type() == PacketType.S2C_CHARACTER_SELECTION_SUCCESS)
            .findFirst()
            .orElse(null);
        
        assertNotNull(successPacket);
        assertInstanceOf(CharacterSelectionSuccess.class, successPacket.payload());
        CharacterSelectionSuccess success = (CharacterSelectionSuccess) successPacket.payload();
        assertEquals("Hero", success.characterName());
        assertEquals(10, success.level());
        assertEquals("zone1", success.zone());
        
        // Verify entity was created in ECS world
        assertFalse(ecsWorld.getAllEntities().spliterator().estimateSize() == 0);
    }
    
    @Test
    void testCharacterSelectionNotAuthenticated() throws Exception {
        // Try to select character without logging in
        CharacterSelectionRequest request = new CharacterSelectionRequest(1L);
        GamePacket packet = new GamePacket(PacketType.C2S_CHARACTER_SELECTION, request);
        
        handler.channelRead0(ctx, packet);
        
        // Verify login failure was sent
        ArgumentCaptor<GamePacket> packetCaptor = ArgumentCaptor.forClass(GamePacket.class);
        verify(ctx).writeAndFlush(packetCaptor.capture());
        
        GamePacket sentPacket = packetCaptor.getValue();
        assertEquals(PacketType.S2C_LOGIN_FAILURE, sentPacket.type());
    }
    
    @Test
    void testCharacterSelectionInvalidCharacter() throws Exception {
        // First, authenticate
        String token = "valid-token";
        String username = "testuser";
        Account mockAccount = Mockito.mock(Account.class);
        
        when(tokenValidationService.validateTokenAndGetUsername(token))
            .thenReturn(Optional.of(username));
        when(accountRepository.existsByUsername(username)).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenReturn(mockAccount);
        when(mockAccount.getId()).thenReturn(1L);
        when(mockAccount.getUsername()).thenReturn(username);
        when(characterRepository.findByAccountId(1L)).thenReturn(new ArrayList<>());
        
        TokenLoginRequest loginRequest = new TokenLoginRequest(token);
        GamePacket loginPacket = new GamePacket(PacketType.C2S_TOKEN_LOGIN_REQUEST, loginRequest);
        handler.channelRead0(ctx, loginPacket);
        
        // Try to select non-existent character
        when(characterRepository.findById(999L)).thenReturn(Optional.empty());
        
        CharacterSelectionRequest selectionRequest = new CharacterSelectionRequest(999L);
        GamePacket selectionPacket = new GamePacket(PacketType.C2S_CHARACTER_SELECTION, selectionRequest);
        handler.channelRead0(ctx, selectionPacket);
        
        // Verify login failure was sent
        ArgumentCaptor<GamePacket> packetCaptor = ArgumentCaptor.forClass(GamePacket.class);
        verify(ctx, atLeast(2)).writeAndFlush(packetCaptor.capture());
        
        // There should be at least 2 packets: character list + login failure
        List<GamePacket> sentPackets = packetCaptor.getAllValues();
        assertTrue(sentPackets.size() >= 2);
    }
    
    @Test
    void testMovementIntent() throws Exception {
        // First authenticate and select character
        setupAuthenticatedSession();
        
        // Send movement intent
        com.grimoire.shared.dto.MovementIntent intent = new com.grimoire.shared.dto.MovementIntent(300.0, 400.0);
        GamePacket packet = new GamePacket(PacketType.C2S_MOVEMENT_INTENT, intent);
        
        handler.channelRead0(ctx, packet);
        
        // Movement should be processed without errors
        assertTrue(true); // If we get here, no exception was thrown
    }
    
    @Test
    void testChatMessage() throws Exception {
        // First authenticate and select character
        setupAuthenticatedSession();
        
        // Send chat message
        ChatMessage chatMsg = new ChatMessage("Hello, world!");
        GamePacket packet = new GamePacket(PacketType.C2S_CHAT_MESSAGE, chatMsg);
        
        handler.channelRead0(ctx, packet);
        
        // Chat should be processed without errors
        assertTrue(true); // If we get here, no exception was thrown
    }
    
    @Test
    void testChannelInactive() throws Exception {
        // First authenticate
        String token = "valid-token";
        String username = "testuser";
        Account mockAccount = Mockito.mock(Account.class);
        
        when(tokenValidationService.validateTokenAndGetUsername(token))
            .thenReturn(Optional.of(username));
        when(accountRepository.existsByUsername(username)).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenReturn(mockAccount);
        when(mockAccount.getId()).thenReturn(1L);
        when(mockAccount.getUsername()).thenReturn(username);
        when(characterRepository.findByAccountId(1L)).thenReturn(new ArrayList<>());
        
        TokenLoginRequest loginRequest = new TokenLoginRequest(token);
        GamePacket loginPacket = new GamePacket(PacketType.C2S_TOKEN_LOGIN_REQUEST, loginRequest);
        handler.channelRead0(ctx, loginPacket);
        
        // Disconnect
        handler.channelInactive(ctx);
        
        // Should complete without errors
        assertTrue(true);
    }
    
    @Test
    void testExceptionCaught() {
        Exception testException = new Exception("Test exception");
        
        handler.exceptionCaught(ctx, testException);
        
        // Should close the channel
        verify(ctx).close();
    }
    
    @Test
    void testUnknownPacketType() throws Exception {
        // Send packet with non-null but unhandled packet type (S2C packet to server)
        GamePacket packet = new GamePacket(PacketType.S2C_GAME_STATE_UPDATE, null);
        
        handler.channelRead0(ctx, packet);
        
        // Should complete without throwing exception - just logs a warning
        assertTrue(true);
    }
    
    private void setupAuthenticatedSession() throws Exception {
        String token = "valid-token";
        String username = "testuser";
        Account mockAccount = Mockito.mock(Account.class);
        Character mockChar = Mockito.mock(Character.class);
        
        when(tokenValidationService.validateTokenAndGetUsername(token))
            .thenReturn(Optional.of(username));
        when(accountRepository.existsByUsername(username)).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenReturn(mockAccount);
        when(mockAccount.getId()).thenReturn(1L);
        when(mockAccount.getUsername()).thenReturn(username);
        when(characterRepository.findByAccountId(1L)).thenReturn(new ArrayList<>());
        
        TokenLoginRequest loginRequest = new TokenLoginRequest(token);
        GamePacket loginPacket = new GamePacket(PacketType.C2S_TOKEN_LOGIN_REQUEST, loginRequest);
        handler.channelRead0(ctx, loginPacket);
        
        // Setup and select character
        when(mockChar.getId()).thenReturn(1L);
        when(mockChar.getName()).thenReturn("Hero");
        when(mockChar.getLevel()).thenReturn(10);
        when(mockChar.getLastZone()).thenReturn("zone1");
        when(mockChar.getLastX()).thenReturn(100.0);
        when(mockChar.getLastY()).thenReturn(200.0);
        when(mockChar.getAccount()).thenReturn(mockAccount);
        when(mockChar.getLastPlayedAt()).thenReturn(LocalDateTime.now());
        
        when(characterRepository.findById(1L)).thenReturn(Optional.of(mockChar));
        when(characterRepository.update(any(Character.class))).thenReturn(mockChar);
        
        CharacterSelectionRequest selectionRequest = new CharacterSelectionRequest(1L);
        GamePacket selectionPacket = new GamePacket(PacketType.C2S_CHARACTER_SELECTION, selectionRequest);
        handler.channelRead0(ctx, selectionPacket);
    }
}
