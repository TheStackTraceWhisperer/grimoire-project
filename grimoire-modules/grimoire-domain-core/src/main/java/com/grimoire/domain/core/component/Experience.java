package com.grimoire.domain.core.component;

/**
 * Experience component tracking current XP and threshold for the next level.
 */
public class Experience implements Component {

    /**
     * Accumulated experience points.
     */
    public int currentXp;

    /**
     * The XP threshold required to advance to the next level.
     */
    public int xpToNextLevel;

    /**
     * No-arg constructor for array pre-allocation.
     */
    public Experience() {
        // default values
    }

    /**
     * Creates an experience component.
     *
     * @param currentXp
     *            accumulated XP
     * @param xpToNextLevel
     *            threshold for the next level
     */
    public Experience(int currentXp, int xpToNextLevel) {
        this.currentXp = currentXp;
        this.xpToNextLevel = xpToNextLevel;
    }

    /**
     * Zero-allocation update.
     *
     * @param newCurrentXp
     *            new current XP
     * @param newXpToNextLevel
     *            new XP threshold
     */
    public void update(int newCurrentXp, int newXpToNextLevel) {
        this.currentXp = newCurrentXp;
        this.xpToNextLevel = newXpToNextLevel;
    }

    /**
     * Adds XP to the current total.
     *
     * @param xpGain
     *            the XP to add
     */
    public void addXp(int xpGain) {
        this.currentXp += xpGain;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Experience e)) {
            return false;
        }
        return currentXp == e.currentXp && xpToNextLevel == e.xpToNextLevel;
    }

    @Override
    public int hashCode() {
        return 31 * currentXp + xpToNextLevel;
    }

    @Override
    public String toString() {
        return "Experience[currentXp=" + currentXp + ", xpToNextLevel=" + xpToNextLevel + "]";
    }
}
