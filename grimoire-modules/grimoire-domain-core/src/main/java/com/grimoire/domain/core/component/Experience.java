package com.grimoire.domain.core.component;

/**
 * Experience component tracking current XP and threshold for the next level.
 *
 * @param currentXp
 *            accumulated experience points
 * @param xpToNextLevel
 *            the XP threshold required to advance to the next level
 */
public record Experience(int currentXp, int xpToNextLevel) implements Component {
}
