package com.grimoire.domain.core.component;

/**
 * Stats component for entity health and combat attributes.
 */
public class Stats implements Component {

    /** Current hit points. */
    public int hp;

    /** Maximum hit points. */
    public int maxHp;

    /** Defense rating (reduces incoming damage). */
    public int defense;

    /** Attack rating (determines outgoing damage). */
    public int attack;

    /** No-arg constructor for array pre-allocation. */
    public Stats() {
        // default values
    }

    /**
     * Creates stats with the given values.
     *
     * @param hp
     *            current hit points
     * @param maxHp
     *            maximum hit points
     * @param defense
     *            defense rating
     * @param attack
     *            attack rating
     */
    public Stats(int hp, int maxHp, int defense, int attack) {
        this.hp = hp;
        this.maxHp = maxHp;
        this.defense = defense;
        this.attack = attack;
    }

    /**
     * Zero-allocation update of all stats.
     *
     * @param newHp
     *            new current HP
     * @param newMaxHp
     *            new max HP
     * @param newDefense
     *            new defense
     * @param newAttack
     *            new attack
     */
    public void update(int newHp, int newMaxHp, int newDefense, int newAttack) {
        this.hp = newHp;
        this.maxHp = newMaxHp;
        this.defense = newDefense;
        this.attack = newAttack;
    }

    /**
     * Applies damage, flooring HP at zero.
     *
     * @param damage
     *            the damage amount
     */
    public void applyDamage(int damage) {
        this.hp = Math.max(0, this.hp - damage);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Stats s)) {
            return false;
        }
        return hp == s.hp && maxHp == s.maxHp && defense == s.defense && attack == s.attack;
    }

    @Override
    public int hashCode() {
        int result = hp;
        result = 31 * result + maxHp;
        result = 31 * result + defense;
        result = 31 * result + attack;
        return result;
    }

    @Override
    public String toString() {
        return "Stats[hp=" + hp + ", maxHp=" + maxHp + ", defense=" + defense + ", attack=" + attack + "]";
    }
}
