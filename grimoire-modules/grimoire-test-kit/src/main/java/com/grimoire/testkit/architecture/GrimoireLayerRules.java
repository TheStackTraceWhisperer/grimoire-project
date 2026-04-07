package com.grimoire.testkit.architecture;

import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;

/**
 * Reusable ArchUnit rule catalog enforcing Grimoire layer boundaries.
 *
 * <p>
 * Consumer modules import these rules in their own {@code @ArchTest} fields.
 * For example:
 * </p>
 *
 * <pre>{@code
 * @ArchTest
 * static final ArchRule domainIsolation = GrimoireLayerRules.DOMAIN_MUST_NOT_IMPORT_INFRA;
 * }</pre>
 *
 * <p>
 * Rules use {@code allowEmptyShould(true)} so they remain valid even when
 * applied to modules with few classes.
 * </p>
 *
 * @see <a href="../../../docs/grimoire-unified-plan.md">Unified Plan §7.3</a>
 */
public final class GrimoireLayerRules {

    /**
     * Contracts modules must not depend on any other grimoire package.
     *
     * <p>
     * {@code com.grimoire.contracts..*} may only import from
     * {@code com.grimoire.contracts..*}, {@code java..*}, and third-party
     * libraries.
     * </p>
     */
    public static final ArchRule CONTRACTS_MUST_NOT_IMPORT_GRIMOIRE = ArchRuleDefinition.noClasses()
            .that().resideInAPackage("com.grimoire.contracts..")
            .should().dependOnClassesThat()
            .resideInAnyPackage(
                    "com.grimoire.domain..",
                    "com.grimoire.application..",
                    "com.grimoire.infra..")
            .allowEmptyShould(true)
            .as("Contracts must not import domain, application, or infra packages");

    /**
     * Domain modules must not depend on infrastructure or application packages.
     *
     * <p>
     * Ensures domain purity: no Netty, no Micronaut, no JPA, no application
     * orchestration.
     * </p>
     */
    public static final ArchRule DOMAIN_MUST_NOT_IMPORT_INFRA = ArchRuleDefinition.noClasses()
            .that().resideInAPackage("com.grimoire.domain..")
            .should().dependOnClassesThat()
            .resideInAnyPackage(
                    "com.grimoire.infra..",
                    "com.grimoire.application..",
                    "io.netty..",
                    "io.micronaut..",
                    "jakarta.persistence..")
            .allowEmptyShould(true)
            .as("Domain must not import infra, application, or framework packages");

    /**
     * Application modules must not depend on infrastructure packages.
     *
     * <p>
     * Application layer depends on domain and contracts only — infrastructure
     * adapters implement application ports, not the other way around.
     * </p>
     */
    public static final ArchRule APPLICATION_MUST_NOT_IMPORT_INFRA = ArchRuleDefinition.noClasses()
            .that().resideInAPackage("com.grimoire.application..")
            .should().dependOnClassesThat()
            .resideInAnyPackage(
                    "com.grimoire.infra..",
                    "io.netty..",
                    "jakarta.persistence..")
            .allowEmptyShould(true)
            .as("Application must not import infra or framework packages");

    /**
     * No class in the grimoire codebase should use {@code java.util.logging}
     * directly — use SLF4J instead.
     */
    public static final ArchRule NO_JAVA_UTIL_LOGGING = ArchRuleDefinition.noClasses()
            .that().resideInAPackage("com.grimoire..")
            .should().dependOnClassesThat()
            .resideInAPackage("java.util.logging..")
            .allowEmptyShould(true)
            .as("Grimoire classes must not use java.util.logging — use SLF4J");

    private GrimoireLayerRules() {
        // utility class
    }
}
