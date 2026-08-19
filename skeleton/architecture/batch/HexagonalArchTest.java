package __BASE_PACKAGE__;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.jmolecules.archunit.JMoleculesArchitectureRules;



@AnalyzeClasses(
    packages = "__BASE_PACKAGE__",
    importOptions = ImportOption.DoNotIncludeTests.class
)
public class HexagonalArchTest {
    @ArchTest
    ArchRule hexagonalArchitecture = JMoleculesArchitectureRules.ensureHexagonal();

    @ArchTest
    static final ArchRule domainModelsMustNotDependOnApplicationBoundaries = noClasses()
        .that().resideInAnyPackage(
            "..application.model..",
            "..aggregation.model.."
        )
        .should().dependOnClassesThat().resideInAnyPackage(
            "..application.port..",
            "..application.service..",
            "..adapter.."
        );

    @ArchTest
    static final ArchRule domainModelsMustNotDependOnInfrastructureFrameworks = noClasses()
        .that().resideInAnyPackage(
            "..application.model..",
            "..aggregation.model.."
        )
        .should().dependOnClassesThat().resideInAnyPackage(
            "org.springframework..",
            "jakarta.persistence..",
            "org.jooq..",
            "com.fasterxml.jackson.."
        );

    @ArchTest
    static final ArchRule inboundAdaptersMustUseInPortsInsteadOfServices = noClasses()
        .that().resideInAPackage("..adapter.in..")
        .should().dependOnClassesThat().resideInAPackage("..application.service..");

    @ArchTest
    static final ArchRule inboundAdaptersMustNotBypassInPortsThroughOutboundAdapters = noClasses()
        .that().resideInAPackage("..adapter.in..")
        .should().dependOnClassesThat().resideInAPackage("..adapter.out..");

    @ArchTest
    static final ArchRule servicesMustUseOutPortsInsteadOfAdapters = noClasses()
        .that().resideInAPackage("..application.service..")
        .should().dependOnClassesThat().resideInAPackage("..adapter..");

    @ArchTest
    static final ArchRule outboundAdaptersMustNotDependOnServices = noClasses()
        .that().resideInAPackage("..adapter.out..")
        .should().dependOnClassesThat().resideInAPackage("..application.service..");

    @ArchTest
    static final ArchRule outboundAdaptersMustNotDependOnInboundAdapters = noClasses()
        .that().resideInAPackage("..adapter.out..")
        .should().dependOnClassesThat().resideInAPackage("..adapter.in..");
}
