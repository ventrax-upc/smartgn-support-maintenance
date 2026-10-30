package com.ventrax.support;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ClassFileImporter;

import org.junit.jupiter.api.Test;

class ArchitectureTest {
    @Test
    void applicationUsesPorts() {
        noClasses()
                .that()
                .resideInAPackage("..application..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage("..infrastructure..", "..interfaces..")
                .check(new ClassFileImporter().importPackages("com.ventrax.support"));
    }
}
