package __BASE_PACKAGE__;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import java.util.Set;

@AnalyzeClasses(
    packages = "__BASE_PACKAGE__",
    importOptions = ImportOption.DoNotIncludeTests.class
)
class VerticalSliceArchTest {

    private static final String ROOT_PACKAGE = "__BASE_PACKAGE__";
    private static final Set<String> SHARED_SLICES = Set.of("aggregation", "batch", "config", "global");

    @ArchTest
    static final ArchRule featureSlicesMustNotDependOnOtherFeatureSlices = classes()
        .that().resideInAPackage(ROOT_PACKAGE + "..")
        .and().resideOutsideOfPackages(
            ROOT_PACKAGE + ".aggregation..",
            ROOT_PACKAGE + ".batch..",
            ROOT_PACKAGE + ".config..",
            ROOT_PACKAGE + ".global.."
        )
        .should(new ArchCondition<>("not depend directly on another feature slice") {
            @Override
            public void check(JavaClass source, ConditionEvents events) {
                String sourceSlice = sliceOf(source);
                source.getDirectDependenciesFromSelf().stream()
                      .map(dependency -> dependency.getTargetClass())
                      .filter(target -> isForbiddenDependency(sourceSlice, target))
                      .forEach(target -> events.add(SimpleConditionEvent.violated(
                          source,
                          source.getFullName() + " depends on " + target.getFullName()
                      )));
            }
        });

    private static boolean isForbiddenDependency(String sourceSlice, JavaClass target) {
        String targetSlice = sliceOf(target);
        return !targetSlice.isEmpty()
            && !targetSlice.equals(sourceSlice)
            && !SHARED_SLICES.contains(targetSlice);
    }

    private static String sliceOf(JavaClass javaClass) {
        String packageName = javaClass.getPackageName();
        if (!packageName.startsWith(ROOT_PACKAGE + ".")) {
            return "";
        }
        String relativePackage = packageName.substring(ROOT_PACKAGE.length() + 1);
        int separatorIndex = relativePackage.indexOf('.');
        return separatorIndex < 0 ? relativePackage : relativePackage.substring(0, separatorIndex);
    }
}
