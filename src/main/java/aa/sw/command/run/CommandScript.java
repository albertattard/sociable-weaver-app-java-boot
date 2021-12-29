package aa.sw.command.run;

import com.google.common.hash.Hashing;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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

    private static String createScript(final String commands) {
        return bashScript()
                .concat(sourceSdkmanInitScript())
                .concat(commands)
                .concat("\n");
    }

    private static String bashScript() {
        return """
                #!/bin/bash
                      
                set -e
                                
                """;
    }

    private static String sourceSdkmanInitScript() {
        return """
                # We need to have this as otherwise the sdk commands will not work.
                # This is a workaround, but the only one that worked.
                [[ -s "$HOME/.sdkman/bin/sdkman-init.sh" ]] && source "$HOME/.sdkman/bin/sdkman-init.sh"
                                
                """;
    }

    private static String createFileName(final String script) {
        return Hashing.sha256()
                .hashString(script, StandardCharsets.UTF_8)
                .toString();
    }

    public WithScriptCreated createScriptIn(final Path directory) {
        requireNonNull(directory);

        if (!Files.isDirectory(directory)) {
            uncheckedIo("Failed to create directory", () -> Files.createDirectories(directory));
        }

        final Path path = directory.resolve(fileName);
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

        return new WithScriptCreated(path);
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class WithScriptCreated {

        private final Path path;

        public <T> T with(final Function<Path, T> mapper, final Runnable onFinally) {
            requireNonNull(onFinally);
            try {
                return with(mapper);
            } finally {
                onFinally.run();
            }
        }

        public <T> T with(final Function<Path, T> mapper) {
            requireNonNull(mapper);
            failIfFileDoesNotExists();

            try {
                return mapper.apply(path);
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
