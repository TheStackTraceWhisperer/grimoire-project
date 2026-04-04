package com.grimoire.domain.combat.component;

import com.grimoire.domain.core.component.Component;

/**
 * Monster component with type classification and XP reward.
 */
public class Monster implements Component {

    /** The type of monster. */
    public MonsterType type;

    /** XP awarded to the killer when this monster dies. */
    public int xpReward;

    /** No-arg constructor for array pre-allocation. */
    public Monster() {
        // default values
    }

    /**
     * Creates a monster with the given type and XP reward.
     *
     * @param type
     *            the monster type
     * @param xpReward
     *            XP awarded on kill
     */
    public Monster(MonsterType type, int xpReward) {
        this.type = type;
        this.xpReward = xpReward;
    }

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
     * Zero-allocation update.
     *
     * @param newType
     *            new monster type
     * @param newXpReward
     *            new XP reward
     */
    public void update(MonsterType newType, int newXpReward) {
        this.type = newType;
        this.xpReward = newXpReward;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Monster m)) {
            return false;
        }
        return xpReward == m.xpReward && type == m.type;
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        return 31 * result + xpReward;
    }

    @Override
    public String toString() {
        return "Monster[type=" + type + ", xpReward=" + xpReward + "]";
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
