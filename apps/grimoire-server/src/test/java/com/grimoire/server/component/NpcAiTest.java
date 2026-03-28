package com.grimoire.server.component;

import com.grimoire.ecs.Component;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NpcAiTest {
    
    @Test
    void testNpcAiCreation() {
        NpcAi npcAi = new NpcAi(NpcAi.AiType.HOSTILE_AGGRO_MELEE);
        
        assertEquals(NpcAi.AiType.HOSTILE_AGGRO_MELEE, npcAi.type());
    }
    
    @Test
    void testNpcAiIsComponent() {
        NpcAi npcAi = new NpcAi(NpcAi.AiType.FRIENDLY_WANDER);
        assertInstanceOf(Component.class, npcAi);
    }
    
    @Test
    void testAllAiTypes() {
        assertNotNull(new NpcAi(NpcAi.AiType.HOSTILE_AGGRO_MELEE));
        assertNotNull(new NpcAi(NpcAi.AiType.FRIENDLY_WANDER));
    }
    
    @Test
    void testAiTypeEnum() {
        assertEquals(2, NpcAi.AiType.values().length);
        assertEquals(NpcAi.AiType.HOSTILE_AGGRO_MELEE, NpcAi.AiType.valueOf("HOSTILE_AGGRO_MELEE"));
        assertEquals(NpcAi.AiType.FRIENDLY_WANDER, NpcAi.AiType.valueOf("FRIENDLY_WANDER"));
    }
}
