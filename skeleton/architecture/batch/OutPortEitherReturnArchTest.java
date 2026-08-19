package __BASE_PACKAGE__;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import __BASE_PACKAGE__.global.annotation.InternalService;
import io.vavr.control.Either;
import org.springframework.stereotype.Service;

@AnalyzeClasses(
    packages = "__BASE_PACKAGE__",
    importOptions = {ImportOption.DoNotIncludeTests.class}
)
class OutPortEitherReturnArchTest {

    @ArchTest
    static final ArchRule outPortMethodsShouldReturnEither = methods()
        .that().areDeclaredInClassesThat().resideInAPackage("..application.port.out..")
        .and().areDeclaredInClassesThat().areInterfaces()
        .and().arePublic()
        .should().haveRawReturnType(Either.class)
        .because("out ports must represent failures explicitly with Either");

    @ArchTest
    static final ArchRule applicationServicesShouldHandleEither = methods()
        .that().areDeclaredInClassesThat().areMetaAnnotatedWith(Service.class)
        .and().areDeclaredInClassesThat().areNotAnnotatedWith(InternalService.class)
        .and().arePublic()
        .should().notHaveRawReturnType(Either.class)
        .because("application services must handle out port failures at the application boundary");

    @ArchTest
    static final ArchRule internalServicesShouldResideInInternalServicePackages = classes()
        .that().areAnnotatedWith(InternalService.class)
        .should().resideInAPackage("..application..service.internal..")
        .because("internal services must remain inside an application service internal package");

    @ArchTest
    static final ArchRule internalServicesShouldOnlyBeUsedByServices = classes()
        .that().areAnnotatedWith(InternalService.class)
        .should().onlyHaveDependentClassesThat().areMetaAnnotatedWith(Service.class)
        .because("internal services may only be used by Spring services");

}
