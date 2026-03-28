//package application;
//
//import engine.EngineTestHarness;
//import engine.services.state.ApplicationStateService;
//import engine.services.world.WorldService;
//import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
//import jakarta.inject.Inject;
//import org.junit.jupiter.api.Test;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//
//// Assuming these components exist in the application module
//import application.components.PlayerComponent;
//import application.components.EnemyComponent;
//
//// Add packages to scan to include both application and engine beans
//@MicronautTest(packages = {"application", "engine"})
//public class PlayingStateIT extends EngineTestHarness {
//
//    @Inject
//    private ApplicationStateService stateService;
//    @Inject
//    private WorldService worldService;
//
//    @Test
//    void onEnter_loadsPlayingSceneAndSystems() {
//        // Given the engine is running
//
//        // When we change to the PlayingState
//        stateService.changeState(PlayingState.class);
//
//        // Then the world should be populated with entities from the playing scene
//        var playerEntities = worldService.getEntitiesWith(PlayerComponent.class);
//        var enemyEntities = worldService.getEntitiesWith(EnemyComponent.class);
//
//        assertEquals(1, playerEntities.size(), "There should be exactly one Player entity.");
//        assertEquals(1, enemyEntities.size(), "There should be exactly one Enemy entity.");
//    }
//}
