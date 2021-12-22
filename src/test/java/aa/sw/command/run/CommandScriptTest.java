package aa.sw.command.run;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static aa.sw.common.UncheckedIo.uncheckedIo;
import static org.assertj.core.api.Assertions.assertThat;

class CommandScriptTest {

    private static final File DIRECTORY = new File("build/command-script");

    @Test
    void handleSingleLineCommands() {
        /* Given */
        final String command = "echo 'hello world'";

        /* When */
        final String contents = createAndReadScript(command);

        /* Then */
        final String expected = """
                ##!/bin/bash
                                
                echo 'hello world'
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
                ##!/bin/bash
                                
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
                .with(script -> {
                    final List<String> list = script.toProcessParameters();
                    final String fileName = list.get(0);
                    final Path file = new File(DIRECTORY, fileName).toPath();
                    return uncheckedIo(() -> Files.readString(file, StandardCharsets.UTF_8));
                });
    }
}