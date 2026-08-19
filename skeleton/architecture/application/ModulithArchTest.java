package __BASE_PACKAGE__;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

public class ModulithArchTest {
    ApplicationModules modules = ApplicationModules.of(__APPLICATION_CLASS__.class);

    @Test
    void verifyModulithStructure() {
        modules.verify();
    }

    @Test
    void writeDocumentationSnippets() {
        new Documenter(modules)
            .writeModulesAsPlantUml()
            .writeIndividualModulesAsPlantUml();
    }
}
