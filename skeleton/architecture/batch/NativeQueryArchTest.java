package __BASE_PACKAGE__;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class NativeQueryArchTest {

    @Test
    void mainCodeMustNotUseNativeQueries() throws IOException {
        Path sourceRoot = sourceRoot();
        List<Path> violations;
        try (var paths = Files.walk(sourceRoot)) {
            violations = paths.filter(path -> path.toString().endsWith(".java"))
                              .filter(NativeQueryArchTest::containsNativeQueryAccess)
                              .toList();
        }

        assertThat(violations)
            .as("Use JPA repositories or jOOQ instead of native SQL in project main code.")
            .isEmpty();
    }

    private static Path sourceRoot() {
        Path current = Path.of("").toAbsolutePath();
        while (current != null) {
            Path moduleSourceRoot = current.resolve("app/src/main/java");
            if (Files.isDirectory(moduleSourceRoot)) {
                return moduleSourceRoot;
            }
            Path projectSourceRoot = current.resolve("src/main/java");
            if (Files.isDirectory(projectSourceRoot)) {
                return projectSourceRoot;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("Cannot resolve project main source root.");
    }

    private static boolean containsNativeQueryAccess(Path path) {
        try {
            String source = Files.readString(path);
            return source.contains("createNativeQuery")
                || source.contains("nativeQuery = true")
                || source.contains("nativeQuery=true")
                || source.contains("JdbcTemplate")
                || source.contains("NamedParameterJdbcTemplate");
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot read " + path, exception);
        }
    }
}
