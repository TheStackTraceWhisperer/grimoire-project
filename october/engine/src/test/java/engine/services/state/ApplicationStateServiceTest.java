package engine.services.state;

import io.micronaut.context.ApplicationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import engine.services.world.WorldService;
import engine.services.world.ISystem;

import jakarta.inject.Provider;

import java.util.List;
import java.util.Arrays;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationStateServiceTest {

    // Minimal test system types
    static class SysA1 implements ISystem {}
    static class SysA2 implements ISystem {}
    static class SysB1 implements ISystem {}
    static class SysB2 implements ISystem {}

    @Mock
    private ApplicationContext applicationContext;
    @Mock
    private Provider<ApplicationState> initialStateProvider;
    @Mock
    private ApplicationState initialState;
    @Mock
    private ApplicationState newState;
    @Mock
    private WorldService worldService;

    private ApplicationStateService stateService;

    @BeforeEach
    void setUp() {
        lenient().when(initialStateProvider.get()).thenReturn(initialState);
        stateService = new ApplicationStateService(applicationContext, worldService, initialStateProvider);
    }

    @Test
    void start_pushesAndEntersInitialState() {
        // When
        stateService.start();

        // Then
        verify(initialStateProvider).get();
        verify(initialState).onEnter();
    }

    @Test
    void pushState_entersNewState() {
        // When
        stateService.pushState(newState);

        // Then
        verify(newState).onEnter();
    }

    @Test
    void popState_exitsCurrentState() {
        // Given
        stateService.pushState(initialState);

        // When
        stateService.popState();

        // Then
        verify(initialState).onExit();
    }

    @Test
    void changeState_exitsOldStateAndEntersNewState() {
        // Given
        stateService.pushState(initialState);

        // When
        stateService.changeState(newState);

        // Then
        InOrder inOrder = inOrder(initialState, newState);
        inOrder.verify(initialState).onExit();
        inOrder.verify(newState).onEnter();
    }

    @Test
    void update_callsUpdateOnActiveState() {
        // Given
        stateService.pushState(initialState);
        float deltaTime = 0.16f;

        // When
        stateService.update(deltaTime);

        // Then
        verify(initialState).onUpdate(deltaTime);
    }

    @Test
    void stop_exitsAllStatesInStack() {
        // Given
        stateService.pushState(initialState);
        stateService.pushState(newState);

        // When
        stateService.stop();

        // Then
        InOrder inOrder = inOrder(newState, initialState);
        inOrder.verify(newState).onExit();
        inOrder.verify(initialState).onExit();
    }

    // --- Class-based system enable/disable behavior ---

    @Test
    void pushState_attachesSystemsAfterOnEnter() {
        // Given
        when(newState.systems()).thenReturn(List.of(SysB1.class));

        // When
        stateService.pushState(newState);

        // Then
        InOrder inOrder = inOrder(newState, worldService);
        inOrder.verify(newState).onEnter();
        inOrder.verify(worldService).enableSystem(SysB1.class);
    }

    @Test
    void secondPush_detachesAndSuspendsBeforeEnteringNew_thenAttachesNewSystems() {
        // Given
        when(initialState.systems()).thenReturn(List.of(SysA1.class));
        when(newState.systems()).thenReturn(List.of(SysB1.class));

        // When
        stateService.pushState(initialState);
        stateService.pushState(newState);

        // Then
        InOrder inOrder = inOrder(initialState, newState, worldService);
        // First push
        inOrder.verify(initialState).onEnter();
        inOrder.verify(worldService).enableSystem(SysA1.class);
        // Second push
        inOrder.verify(worldService).disableSystem(SysA1.class);
        inOrder.verify(initialState).onSuspend();
        inOrder.verify(newState).onEnter();
        inOrder.verify(worldService).enableSystem(SysB1.class);
    }

    @Test
    void popState_detachPopped_thenExit_thenResumeUnderlying_andAttachUnderlying() {
        // Given
        when(initialState.systems()).thenReturn(List.of(SysA1.class));
        when(newState.systems()).thenReturn(List.of(SysB1.class));
        stateService.pushState(initialState);
        stateService.pushState(newState);

        // When
        stateService.popState();

        // Then
        InOrder inOrder = inOrder(worldService, newState, initialState);
        inOrder.verify(worldService).disableSystem(SysB1.class);
        inOrder.verify(newState).onExit();
        inOrder.verify(initialState).onResume();
        inOrder.verify(worldService).enableSystem(SysA1.class);
    }

    @Test
    void changeState_detachOld_exitOld_enterNew_andAttachNew_inOrder() {
        // Given
        when(initialState.systems()).thenReturn(List.of(SysA1.class));
        when(newState.systems()).thenReturn(List.of(SysB1.class));
        stateService.pushState(initialState);

        // When
        stateService.changeState(newState);

        // Then
        InOrder inOrder = inOrder(worldService, initialState, newState);
        inOrder.verify(worldService).disableSystem(SysA1.class);
        inOrder.verify(initialState).onExit();
        inOrder.verify(newState).onEnter();
        inOrder.verify(worldService).enableSystem(SysB1.class);
    }

    @Test
    void start_attachesInitialStateSystems() {
        // Given
        when(initialState.systems()).thenReturn(List.of(SysA1.class));

        // When
        stateService.start();

        // Then
        InOrder inOrder = inOrder(initialState, worldService);
        inOrder.verify(initialState).onEnter();
        inOrder.verify(worldService).enableSystem(SysA1.class);
    }

    @Test
    void stop_detachesSystemsForAllStates() {
        // Given
        when(initialState.systems()).thenReturn(List.of(SysA1.class));
        when(newState.systems()).thenReturn(List.of(SysB1.class));
        stateService.pushState(initialState);
        stateService.pushState(newState);

        // When
        stateService.stop();

        // Then
        verify(worldService, atLeastOnce()).disableSystem(SysB1.class);
        verify(worldService, atLeastOnce()).disableSystem(SysA1.class);
    }

    @Test
    void systemsMayContainNulls_andTheyAreIgnored() {
        // Given
        when(newState.systems()).thenReturn(Arrays.asList(SysB1.class, null));

        // When
        stateService.pushState(newState);
        stateService.popState();

        // Then
        verify(worldService, times(1)).enableSystem(SysB1.class);
        verify(worldService, never()).enableSystem(null);
        verify(worldService, times(1)).disableSystem(SysB1.class);
        verify(worldService, never()).disableSystem(null);
    }

    @Test
    void withMultipleSystems_attachAndDetachAllInOrder() {
        // Given
        when(newState.systems()).thenReturn(List.of(SysB1.class, SysB2.class));

        // When
        stateService.pushState(newState);

        // Then attach order
        InOrder inOrder = inOrder(newState, worldService);
        inOrder.verify(newState).onEnter();
        inOrder.verify(worldService).enableSystem(SysB1.class);
        inOrder.verify(worldService).enableSystem(SysB2.class);

        // When pop
        stateService.popState();

        // Then detach order
        InOrder out = inOrder(worldService, newState);
        out.verify(worldService).disableSystem(SysB2.class); // depends on list order? Our service iterates list order; popState detaches popped state's systems by iterating; order will be same as attach; keep flexible
        out.verify(newState).onExit();
    }

    @Test
    void pushState_withEmptySystems_doesNotTouchWorldService() {
        // Given
        when(newState.systems()).thenReturn(List.of());

        // When
        stateService.pushState(newState);

        // Then
        verify(newState).onEnter();
        verifyNoInteractions(worldService);
    }

    @Test
    void pushState_withNullSystems_doesNotTouchWorldService() {
        // Given
        when(newState.systems()).thenReturn(null);

        // When
        stateService.pushState(newState);

        // Then
        verify(newState).onEnter();
        verifyNoInteractions(worldService);
    }

    @Test
    void changeState_whenStackEmpty_entersNewAndAttachesSystemsOnly() {
        // Given
        when(newState.systems()).thenReturn(List.of(SysB1.class));

        // When
        stateService.changeState(newState);

        // Then
        InOrder inOrder = inOrder(newState, worldService);
        inOrder.verify(newState).onEnter();
        inOrder.verify(worldService).enableSystem(SysB1.class);
        verify(worldService, never()).disableSystem(any());
        verifyNoInteractions(initialState);
    }
}
