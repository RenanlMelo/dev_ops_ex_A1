package com.example.devopsac1.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

class ModuleBoundariesTest {
    private final JavaClasses classes = new ClassFileImporter().importPackages("com.example.devopsac1");

    @Test
    void controllersDoNotDependOnRepositories() {
        noClasses().that().haveSimpleNameEndingWith("Controller")
                .should().dependOnClassesThat().haveSimpleNameEndingWith("Repository")
                .check(classes);
    }

    @Test
    void publicApisDoNotDependOnInternalImplementations() {
        noClasses().that().resideInAPackage("..api..")
                .should().dependOnClassesThat().resideInAPackage("..internal..")
                .check(classes);
    }

    @Test
    void courseAndStudentDoNotDependOnOtherModulesInternals() {
        noClasses().that().resideInAnyPackage("..curso..", "..aluno..")
                .should().dependOnClassesThat().resideInAPackage("..matricula.internal..")
                .check(classes);
        noClasses().that().resideInAPackage("..curso..")
                .should().dependOnClassesThat().resideInAPackage("..aluno.internal..")
                .check(classes);
        noClasses().that().resideInAPackage("..aluno..")
                .should().dependOnClassesThat().resideInAPackage("..curso.internal..")
                .check(classes);
    }

    @Test
    void enrollmentUsesOnlyPublicContractsOfOtherModules() {
        noClasses().that().resideInAPackage("..matricula..")
                .should().dependOnClassesThat().resideInAnyPackage("..curso.internal..", "..aluno.internal..")
                .check(classes);
    }

    @Test
    void allModulesRespectEveryOtherModulesInternalBoundary() {
        String[] modules = {"curso", "aluno", "matricula", "ranking", "conquista", "carteira"};
        for (String source : modules) {
            for (String target : modules) {
                if (!source.equals(target)) {
                    noClasses().that().resideInAPackage(".." + source + "..")
                            .should().dependOnClassesThat().resideInAPackage(".." + target + ".internal..")
                            .check(classes);
                }
            }
        }
    }

    @Test
    void modulesHaveNoDependencyCycles() {
        slices().matching("com.example.devopsac1.(*)..").should().beFreeOfCycles().check(classes);
    }
}
