//package application;
//
//import engine.EngineTestHarness;
//import engine.services.event.EventPublisherService;
//import engine.services.state.ApplicationStateService;
//import engine.services.world.WorldService;
//import engine.services.world.components.UIButtonComponent;
//import engine.services.world.components.UIImageComponent;
//import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
//import jakarta.inject.Inject;
//import org.junit.jupiter.api.Test;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//// Add packages to scan to include both application and engine beans
//@MicronautTest(packages = {"application", "engine"})
//public class MainMenuStateIT extends EngineTestHarness {
//
//    @Inject
//    private ApplicationStateService stateService;
//    @Inject
//    private WorldService worldService;
//    @Inject
//    private EventPublisherService eventPublisher;
//
//    @Test
//    void onEnter_loadsMainMenuScene() {
//        // Given the engine is running
//
//        // When we change to the MainMenuState
//        stateService.changeState(MainMenuState.class);
//
//        // Then the world should be populated with entities from the scene file
//        var uiImages = worldService.getEntitiesWith(UIImageComponent.class);
//        var uiButtons = worldService.getEntitiesWith(UIButtonComponent.class);
//
//        assertEquals(1, uiImages.size(), "There should be one UI image (the background).");
//        assertEquals(1, uiButtons.size(), "There should be one UI button (the start button).");
//    }
//
//    @Test
//    void onStartGameEvent_changesStateToPlayingState() {
//        // Given the MainMenuState is active
//        stateService.changeState(MainMenuState.class);
//        assertTrue(stateService.peek() instanceof MainMenuState, "Initial state should be MainMenuState.");
//
//        // When we publish the START_NEW_GAME event
//        eventPublisher.publish("START_NEW_GAME");
//
//        // Then the application state should change to PlayingState
//        assertTrue(stateService.peek() instanceof PlayingState, "State should have changed to PlayingState.");
//    }
//}
