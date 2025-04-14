package com.sportclub.challenge.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

class ArchitectureTest {
    private static final String ROOT_PACKAGE = "com.sportclub.challenge";

    private static final String DOMAIN = "Domain";
    private static final String APPLICATION = "Application";
    private static final String INFRASTRUCTURE = "Infrastructure";
    private static final String ADAPTER = "Adapter";

    private static final String DOMAIN_PACKAGE = "..domain..";
    private static final String APPLICATION_PACKAGE = "..application..";
    private static final String INFRASTRUCTURE_PACKAGE = "..infrastructure..";
    private static final String ADAPTER_PACKAGE = "..adapter..";

    private static JavaClasses importedClasses;

    @BeforeAll
    static void setUp() {
        importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(ROOT_PACKAGE);
    }

    @Nested
    @DisplayName("Domain Layer Rules")
    class DomainLayer {
        @Test
        @DisplayName("Domain should only depend on domain and standard Java libraries")
        void domain_should_only_depend_on_domain_and_java() {
            classes().that().resideInAPackage(DOMAIN_PACKAGE)
                    .should().onlyDependOnClassesThat().resideInAnyPackage(
                            DOMAIN_PACKAGE,
                            "java..",
                            "jakarta.validation..",
                            "lombok.."
                    )
                    .check(importedClasses);
        }
    }

    @Nested
    @DisplayName("Application Layer Rules")
    class ApplicationLayer {
        @Test
        @DisplayName("Application should only depend on domain, its ports, and allowed libs")
        void application_should_only_depend_on_domain_ports_and_libs() {
            classes().that().resideInAPackage(APPLICATION_PACKAGE)
                    .should().onlyDependOnClassesThat().resideInAnyPackage(
                            APPLICATION_PACKAGE,
                            DOMAIN_PACKAGE,
                            "java..",
                            "org.slf4j..",
                            "jakarta.validation..",
                            "org.springframework.data.domain..",
                            "org.springframework.stereotype..",
                            "org.springframework.transaction.annotation..",
                            "lombok.."
                    )
                    .check(importedClasses);
        }
    }


    @Nested
    @DisplayName("Adapter Layer Rules")
    class AdapterLayer {
        @Test
        @DisplayName("Adapters should not be depended upon by domain or application")
        void adapters_should_not_be_depended_upon_by_core() {
            noClasses().that().resideInAnyPackage(DOMAIN_PACKAGE, APPLICATION_PACKAGE)
                    .should().dependOnClassesThat().resideInAPackage(ADAPTER_PACKAGE)
                    .check(importedClasses);
        }
    }

    @Nested
    @DisplayName("Layered Architecture Strict Access Rules Check")
    class LayeredArchitectureStrictAccess {

        @Test
        @DisplayName("Check access rules between layers using only specific methods")
        void check_strict_access_rules() {
            layeredArchitecture()
                    .consideringAllDependencies()
                    .layer(DOMAIN).definedBy(DOMAIN_PACKAGE)
                    .layer(APPLICATION).definedBy(APPLICATION_PACKAGE)
                    .layer(INFRASTRUCTURE).definedBy(INFRASTRUCTURE_PACKAGE)
                    .layer(ADAPTER).definedBy(ADAPTER_PACKAGE)
                    .whereLayer(DOMAIN).mayOnlyBeAccessedByLayers(APPLICATION, ADAPTER, INFRASTRUCTURE)
                    .whereLayer(APPLICATION).mayOnlyBeAccessedByLayers(ADAPTER, INFRASTRUCTURE)
                    .whereLayer(ADAPTER).mayOnlyBeAccessedByLayers(ADAPTER, INFRASTRUCTURE)
                    .whereLayer(INFRASTRUCTURE).mayOnlyBeAccessedByLayers(ADAPTER, INFRASTRUCTURE)
                    .check(importedClasses);
        }
    }
}