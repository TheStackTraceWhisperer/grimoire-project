package com.grimoire.domain.combat.component;

import com.grimoire.domain.core.component.Component;

/**
 * Monster component with type classification and XP reward.
 *
 * @param type
 *            the type of monster
 * @param xpReward
 *            XP awarded to the killer when this monster dies
 */
public record Monster(MonsterType type, int xpReward) implements Component {

    /**
     * Creates a monster with the default XP reward for its type.
     *
     * @param type
     *            the monster type
     */
    public Monster(MonsterType type) {
        this(type, type.defaultXp());
    }

    /**
     * Monster type classification with default XP values.
     */
    public enum MonsterType {
        RAT(10), BAT(15), WOLF(25), SKELETON(40);

        /** The default experience points awarded for killing this monster type. */
        private final int xpValue;

        MonsterType(int xpValue) {
            this.xpValue = xpValue;
        }

        /**
         * Returns the default XP reward for this monster type.
         *
         * @return the default XP value
         */
        public int defaultXp() {
            return xpValue;
        }
    }
}
