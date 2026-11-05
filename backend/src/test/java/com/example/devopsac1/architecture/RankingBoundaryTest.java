package com.example.devopsac1.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

class RankingBoundaryTest {
    @Test
    void rankingUsesPublicContractsAndAddsNoModuleCycles() {
        var classes = new ClassFileImporter().importPackages("com.example.devopsac1");
        noClasses().that().resideInAPackage("..ranking..")
                .should().dependOnClassesThat().resideInAnyPackage("..aluno.internal..", "..matricula.internal..", "..curso.internal..")
                .check(classes);
        noClasses().that().resideInAPackage("..matricula..")
                .should().dependOnClassesThat().resideInAPackage("..ranking.internal..").check(classes);
        slices().matching("com.example.devopsac1.(*)..").should().beFreeOfCycles().check(classes);
    }
}
