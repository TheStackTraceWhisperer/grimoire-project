package com.grimoire.server.component;

/**
 * Experience component tracking current XP and threshold to next level.
 */
public record Experience(int currentXp, int xpToNextLevel) implements Component {
}
