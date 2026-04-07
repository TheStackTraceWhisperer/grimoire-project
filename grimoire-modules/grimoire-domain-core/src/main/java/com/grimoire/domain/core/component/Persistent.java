package com.grimoire.domain.core.component;

/**
 * Persistent component associating an entity with a player account.
 */
public class Persistent implements Component {

    /**
     * The external account identifier (e.g. Keycloak subject).
     */
    public String accountId;

    /**
     * No-arg constructor for array pre-allocation.
     */
    public Persistent() {
        // default values
    }

    /**
     * Creates a persistent component.
     *
     * @param accountId
     *            the external account identifier
     */
    public Persistent(String accountId) {
        this.accountId = accountId;
    }

    /**
     * Zero-allocation update.
     *
     * @param newAccountId
     *            the new account identifier
     */
    public void update(String newAccountId) {
        this.accountId = newAccountId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Persistent p)) {
            return false;
        }
        return java.util.Objects.equals(accountId, p.accountId);
    }

    @Override
    public int hashCode() {
        return accountId != null ? accountId.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Persistent[accountId=" + accountId + "]";
    }
}
