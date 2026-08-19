package __BASE_PACKAGE__;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.jmolecules.archunit.JMoleculesDddRules;

@AnalyzeClasses(packages = "__BASE_PACKAGE__")
public class DddArchTest {

    @ArchTest
    ArchRule dddArchitecture = JMoleculesDddRules.all();
}
