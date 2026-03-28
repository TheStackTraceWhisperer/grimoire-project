package com.grimoire.server.component;
import com.grimoire.ecs.Component;

/**
 * Monster component with type classification and XP reward.
 * 
 * @param type the type of monster
 * @param xpReward XP awarded to the killer when this monster dies
 */
public record Monster(MonsterType type, int xpReward) implements Component {
    
    /**
     * Creates a monster with default XP reward based on type.
     */
    public Monster(MonsterType type) {
        this(type, type.getDefaultXp());
    }
    
    public enum MonsterType {
        RAT(10),
        WOLF(25),
        BAT(15),
        SKELETON(40);
        
        private final int defaultXp;
        
        MonsterType(int defaultXp) {
            this.defaultXp = defaultXp;
        }
        
        public int getDefaultXp() {
            return defaultXp;
        }
    }
}
