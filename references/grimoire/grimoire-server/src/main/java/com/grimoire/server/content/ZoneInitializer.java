package com.grimoire.server.content;

import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.runtime.event.ApplicationStartupEvent;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Initializes all zones with NPCs, monsters, and portals on startup.
 */
@Singleton
@RequiredArgsConstructor
@Slf4j
public class ZoneInitializer implements ApplicationEventListener<ApplicationStartupEvent> {
    
    private final NpcFactory npcFactory;
    
    @Override
    public void onApplicationEvent(ApplicationStartupEvent event) {
        log.info("Initializing game zones...");
        
        initializeZone1();
        initializeZone2();
        initializeZone3();
        initializeZone4();
        initializeZone5();
        
        log.info("Zone initialization complete");
    }
    
    private void initializeZone1() {
        // Starting zone with friendly NPCs and easy monsters
        npcFactory.createFriendlyNpc("zone1", 150, 150, "Village Elder");
        npcFactory.createFriendlyNpc("zone1", 200, 180, "Merchant");
        
        npcFactory.createRat("zone1", 300, 100);
        npcFactory.createRat("zone1", 350, 120);
        npcFactory.createRat("zone1", 320, 180);
        
        // Portal to zone2
        npcFactory.createPortal("zone1", 500, 250, "portal-to-zone2", "zone2", "portal-from-zone1");
    }
    
    private void initializeZone2() {
        // Forest zone with wolves
        npcFactory.createWolf("zone2", 200, 200);
        npcFactory.createWolf("zone2", 300, 250);
        npcFactory.createWolf("zone2", 400, 180);
        
        npcFactory.createRat("zone2", 150, 300);
        npcFactory.createRat("zone2", 180, 320);
        
        // Portals
        npcFactory.createPortal("zone2", 100, 100, "portal-from-zone1", "zone1", "portal-to-zone2");
        npcFactory.createPortal("zone2", 500, 300, "portal-to-zone3", "zone3", "portal-from-zone2");
    }
    
    private void initializeZone3() {
        // Cave zone with bats
        npcFactory.createBat("zone3", 200, 150);
        npcFactory.createBat("zone3", 250, 200);
        npcFactory.createBat("zone3", 300, 180);
        npcFactory.createBat("zone3", 350, 220);
        
        npcFactory.createWolf("zone3", 400, 300);
        
        // Portals
        npcFactory.createPortal("zone3", 100, 200, "portal-from-zone2", "zone2", "portal-to-zone3");
        npcFactory.createPortal("zone3", 500, 200, "portal-to-zone4", "zone4", "portal-from-zone3");
    }
    
    private void initializeZone4() {
        // Graveyard zone with skeletons
        npcFactory.createSkeleton("zone4", 200, 200);
        npcFactory.createSkeleton("zone4", 300, 250);
        npcFactory.createSkeleton("zone4", 400, 200);
        
        npcFactory.createBat("zone4", 150, 150);
        npcFactory.createBat("zone4", 450, 150);
        
        // Portals
        npcFactory.createPortal("zone4", 100, 200, "portal-from-zone3", "zone3", "portal-to-zone4");
        npcFactory.createPortal("zone4", 500, 200, "portal-to-zone5", "zone5", "portal-from-zone4");
    }
    
    private void initializeZone5() {
        // Boss zone with multiple enemies
        npcFactory.createSkeleton("zone5", 300, 300);
        npcFactory.createSkeleton("zone5", 350, 320);
        
        npcFactory.createWolf("zone5", 200, 250);
        npcFactory.createWolf("zone5", 400, 250);
        
        npcFactory.createBat("zone5", 250, 200);
        npcFactory.createBat("zone5", 350, 200);
        
        // Portal back to zone4
        npcFactory.createPortal("zone5", 100, 300, "portal-from-zone4", "zone4", "portal-to-zone5");
    }
}
