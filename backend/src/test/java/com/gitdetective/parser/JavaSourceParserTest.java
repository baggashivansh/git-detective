package com.gitdetective.parser;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class JavaSourceParserTest {

    private final JavaSourceParser parser = new JavaSourceParser();

    @TempDir Path tempDir;

    @Test
    @DisplayName("parses package, imports, class, methods, and fields without explanations")
    void parsesJavaMetadata() throws Exception {
        Path javaFile = tempDir.resolve("Sample.java");
        Files.writeString(
                javaFile,
                """
                package com.example;

                import java.util.List;

                public class Sample extends Base implements Runnable {
                    private int count;

                    public Sample() {}

                    public List<String> names() {
                        return List.of();
                    }

                    public void run() {}
                }
                """);

        ParsedJavaFile parsed = parser.parse(javaFile);

        assertThat(parsed.packageName()).isEqualTo("com.example");
        assertThat(parsed.imports())
                .extracting(ParsedJavaFile.ImportInfo::name)
                .contains("java.util.List");
        assertThat(parsed.types()).hasSize(1);
        ParsedJavaFile.TypeInfo type = parsed.types().getFirst();
        assertThat(type.name()).isEqualTo("Sample");
        assertThat(type.kind()).isEqualTo("CLASS");
        assertThat(type.superclassName()).contains("Base");
        assertThat(type.implementedInterfaces()).contains("Runnable");
        assertThat(type.methods())
                .extracting(ParsedJavaFile.MethodInfo::name)
                .contains("Sample", "names", "run");
        assertThat(type.fields()).extracting(ParsedJavaFile.FieldInfo::name).contains("count");
    }
}
