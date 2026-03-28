package engine;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.GeneralCodingRules.*;

/**
 * Architectural tests using ArchUnit to enforce coding standards and architectural rules.
 */
class ArchitectureTest {

    private static JavaClasses importedClasses;

    @BeforeAll
    static void setup() {
        importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("engine");
    }

    @Test
    void services_should_not_depend_on_systems() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("engine.services..")
                .and().resideOutsideOfPackage("engine.services.world.systems..")
                .should().dependOnClassesThat().resideInAPackage("engine.services.world.systems..")
                .because("Services should not depend on system classes to maintain clean architecture");

        rule.check(importedClasses);
    }

    @Test
    void no_classes_should_use_java_util_logging() {
        NO_CLASSES_SHOULD_USE_JAVA_UTIL_LOGGING.check(importedClasses);
    }

    @Test
    void no_classes_should_access_standard_streams() {
        NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS.check(importedClasses);
    }

    @Test
    void no_classes_should_use_field_injection() {
        NO_CLASSES_SHOULD_USE_FIELD_INJECTION.check(importedClasses);
    }

    @Test
    void service_classes_should_be_annotated_with_singleton_or_prototype() {
        ArchRule rule = classes()
                .that().resideInAPackage("engine.services..")
                .and().haveSimpleNameEndingWith("Service")
                .should().beAnnotatedWith("jakarta.inject.Singleton")
                .orShould().beAnnotatedWith("io.micronaut.context.annotation.Prototype")
                .because("Service classes should be properly managed by the dependency injection framework");

        rule.check(importedClasses);
    }

    @Test
    void system_classes_should_be_annotated_with_prototype() {
        ArchRule rule = classes()
                .that().resideInAPackage("engine.services.world.systems..")
                .and().haveSimpleNameEndingWith("System")
                .should().beAnnotatedWith("io.micronaut.context.annotation.Prototype")
                .because("System classes should be prototypes to allow multiple instances per world");

        rule.check(importedClasses);
    }
}
