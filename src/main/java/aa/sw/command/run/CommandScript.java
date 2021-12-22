package aa.sw.command.run;

import com.google.common.hash.Hashing;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;

import static aa.sw.common.UncheckedIo.quietIo;
import static aa.sw.common.UncheckedIo.uncheckedIo;
import static java.util.Objects.requireNonNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CommandScript {

    String script;
    String fileName;

    public static CommandScript of(final String commands) {
        final String script = createScript(commands);
        final String fileName = createFileName(script);

        return new CommandScript(script, fileName);
    }

    public List<String> toProcessParameters() {
        return List.of("./".concat(fileName));
    }

    private static String createScript(final String commands) {
        return """
                ##!/bin/bash
                                
                {COMMANDS}
                """
                .replaceAll("\\{COMMANDS}", commands);
    }

    private static String createFileName(final String script) {
        return Hashing.sha256()
                .hashString(script, StandardCharsets.UTF_8)
                .toString();
    }

    public WithScriptCreated createScriptIn(final File directory) {
        requireNonNull(directory);

        if (!directory.isDirectory() && !directory.mkdirs()) {
            throw new IllegalArgumentException("Failed to create directory");
        }

        final Path path = directory.toPath().resolve(fileName);

        try {
            final String file = uncheckedIo("Failed to create script", () -> {
                Files.writeString(path, script, StandardCharsets.UTF_8);
                return Files.readString(path, StandardCharsets.UTF_8);
            });

            if (!script.equals(file)) {
                throw new RuntimeException("The file content is different from what was expected");
            }

            final boolean executable = path.toFile().setExecutable(true);
            if (!executable) {
                throw new RuntimeException("Failed to mark the script as executable");
            }
        } catch (final RuntimeException e) {
            /* Delete the file if an error occurs while creating it */
            quietIo(() -> Files.deleteIfExists(path));
            throw e;
        }

        return new WithScriptCreated(this, path);
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class WithScriptCreated {

        private final CommandScript script;
        private final Path path;

        public <T> T with(final Function<CommandScript, T> mapper) {
            failIfFileDoesNotExists();

            try {
                return mapper.apply(script);
            } finally {
                quietIo(() -> Files.deleteIfExists(path));
            }
        }

        private void failIfFileDoesNotExists() {
            if (!Files.isRegularFile(path)) {
                throw new IllegalStateException("The file does not exists");
            }
        }
    }
}
