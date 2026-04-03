package com.grimoire.testkit.architecture;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Smoke tests verifying that all ArchUnit rule constants in
 * {@link GrimoireLayerRules} are well-formed and non-null.
 */
class GrimoireLayerRulesTest {

    @Test
    void contractsRuleIsDefinedAndDescribed() {
        assertThat(GrimoireLayerRules.CONTRACTS_MUST_NOT_IMPORT_GRIMOIRE)
                .isNotNull();
        assertThat(GrimoireLayerRules.CONTRACTS_MUST_NOT_IMPORT_GRIMOIRE.getDescription())
                .contains("Contracts");
    }

    @Test
    void domainRuleIsDefinedAndDescribed() {
        assertThat(GrimoireLayerRules.DOMAIN_MUST_NOT_IMPORT_INFRA)
                .isNotNull();
        assertThat(GrimoireLayerRules.DOMAIN_MUST_NOT_IMPORT_INFRA.getDescription())
                .contains("Domain");
    }

    @Test
    void applicationRuleIsDefinedAndDescribed() {
        assertThat(GrimoireLayerRules.APPLICATION_MUST_NOT_IMPORT_INFRA)
                .isNotNull();
        assertThat(GrimoireLayerRules.APPLICATION_MUST_NOT_IMPORT_INFRA.getDescription())
                .contains("Application");
    }

    @Test
    void noJavaUtilLoggingRuleIsDefinedAndDescribed() {
        assertThat(GrimoireLayerRules.NO_JAVA_UTIL_LOGGING)
                .isNotNull();
        assertThat(GrimoireLayerRules.NO_JAVA_UTIL_LOGGING.getDescription())
                .contains("java.util.logging");
    }
}
