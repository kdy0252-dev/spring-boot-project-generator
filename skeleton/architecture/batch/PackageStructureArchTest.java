package __BASE_PACKAGE__;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import jakarta.persistence.Embeddable;
import jakarta.persistence.MappedSuperclass;
import org.jmolecules.architecture.hexagonal.Adapter;
import org.springframework.data.repository.Repository;

@AnalyzeClasses(
    packages = "__BASE_PACKAGE__",
    importOptions = ImportOption.DoNotIncludeTests.class
)
class PackageStructureArchTest {

    private static final DescribedPredicate<JavaClass> ADAPTER_BOUNDARY_TYPE_NAMES =
        new DescribedPredicate<>("have an adapter boundary type name") {
            @Override
            public boolean test(JavaClass javaClass) {
                String simpleName = javaClass.getSimpleName();
                return simpleName.endsWith("Controller")
                    || simpleName.endsWith("PersistenceAdapter")
                    || simpleName.endsWith("ModuleAdapter")
                    || simpleName.endsWith("Job")
                    || simpleName.endsWith("ExceptionHandler");
            }
        };

    @ArchTest
    static final ArchRule jpaEntitiesMustResideInJpaEntityPackages = classes()
        .that().areAnnotatedWith(jakarta.persistence.Entity.class)
        .should().resideInAPackage("..adapter.out.persistence.entity");

    @ArchTest
    static final ArchRule embeddablesMustResideInJpaEntityPackages = classes()
        .that().areAnnotatedWith(Embeddable.class)
        .should().resideInAPackage("..adapter.out.persistence.entity..")
        .allowEmptyShould(true);

    @ArchTest
    static final ArchRule mappedSuperclassesMustResideInJpaEntityPackages = classes()
        .that().areAnnotatedWith(MappedSuperclass.class)
        .should().resideInAPackage("..adapter.out.persistence.entity..");

    @ArchTest
    static final ArchRule jpaEntityTypesMustDeclareEntity = classes()
        .that().haveSimpleNameEndingWith("JpaEntity")
        .and().areNotAnnotatedWith(MappedSuperclass.class)
        .should().beAnnotatedWith(jakarta.persistence.Entity.class);

    @ArchTest
    static final ArchRule springDataRepositoriesMustResideInRepositoryPackages = classes()
        .that().areAssignableTo(Repository.class)
        .should().resideInAPackage("..adapter.out.persistence.repository");

    @ArchTest
    static final ArchRule adapterAnnotationsMustOnlyMarkAdapterBoundaryTypes = classes()
        .that().areMetaAnnotatedWith(Adapter.class)
        .should().resideInAPackage("..adapter..");

    @ArchTest
    static final ArchRule boundaryTypesMustDeclareAdapter = classes()
        .that().resideInAPackage("..adapter..")
        .and(ADAPTER_BOUNDARY_TYPE_NAMES)
        .should().beMetaAnnotatedWith(Adapter.class);
}
