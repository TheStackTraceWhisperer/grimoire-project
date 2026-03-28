package com.grimoire.server.component;

import com.grimoire.ecs.Component;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MonsterTest {
    
    @Test
    void testMonsterCreation() {
        Monster monster = new Monster(Monster.MonsterType.RAT);
        
        assertEquals(Monster.MonsterType.RAT, monster.type());
    }
    
    @Test
    void testMonsterIsComponent() {
        Monster monster = new Monster(Monster.MonsterType.WOLF);
        assertInstanceOf(Component.class, monster);
    }
    
    @Test
    void testAllMonsterTypes() {
        assertNotNull(new Monster(Monster.MonsterType.RAT));
        assertNotNull(new Monster(Monster.MonsterType.WOLF));
        assertNotNull(new Monster(Monster.MonsterType.BAT));
        assertNotNull(new Monster(Monster.MonsterType.SKELETON));
    }
    
    @Test
    void testMonsterTypeEnum() {
        assertEquals(4, Monster.MonsterType.values().length);
        assertEquals(Monster.MonsterType.RAT, Monster.MonsterType.valueOf("RAT"));
        assertEquals(Monster.MonsterType.WOLF, Monster.MonsterType.valueOf("WOLF"));
        assertEquals(Monster.MonsterType.BAT, Monster.MonsterType.valueOf("BAT"));
        assertEquals(Monster.MonsterType.SKELETON, Monster.MonsterType.valueOf("SKELETON"));
    }
}
