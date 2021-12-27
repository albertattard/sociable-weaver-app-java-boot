package aa.sw.command.run;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static aa.sw.common.UncheckedIo.uncheckedIo;
import static org.assertj.core.api.Assertions.assertThat;

class CommandScriptTest {

    private static final Path DIRECTORY = Path.of("build", "command-script");

    @Test
    void handleSingleLineCommands() {
        /* Given */
        final String command = "echo 'hello world'";

        /* When */
        final String contents = createAndReadScript(command);

        /* Then */
        final String expected = """
                #!/bin/bash
                
                echo 'hello world'
                """;
        assertThat(contents)
                .isEqualTo(expected);
    }

    @Test
    void sourceSdkmanScriptWhenSdkCommandIsUsed() {
        /* Given */
        final String command = "sdk install java 17.0.1-oracle";

        /* When */
        final String contents = createAndReadScript(command);

        /* Then */
        final String expected = """
                #!/bin/bash

                # We need to have this as otherwise the sdk commands will not work.
                # This is a workaround, but the only one that worked.
                [[ -s "$HOME/.sdkman/bin/sdkman-init.sh" ]] && source "$HOME/.sdkman/bin/sdkman-init.sh"
                
                sdk install java 17.0.1-oracle
                """;
        assertThat(contents)
                .isEqualTo(expected);
    }

    @Test
    void handleMultilineCommands() {
        /* Given */
        final String command = """
                hub create \\
                  --private \\
                  --description 'Hello World Application (Java + Git + Gradle + Docker + GitHub Actions)' \\
                  'programming--hello-world'\
                """;

        /* When */
        final String contents = createAndReadScript(command);

        /* Then */
        final String expected = """
                #!/bin/bash
                
                hub create \\
                  --private \\
                  --description 'Hello World Application (Java + Git + Gradle + Docker + GitHub Actions)' \\
                  'programming--hello-world'
                """;
        assertThat(contents)
                .isEqualTo(expected);
    }

    private String createAndReadScript(final String command) {
        return CommandScript.of(command)
                .createScriptIn(DIRECTORY)
                .with(path -> uncheckedIo(() -> Files.readString(path, StandardCharsets.UTF_8)));
    }
}
