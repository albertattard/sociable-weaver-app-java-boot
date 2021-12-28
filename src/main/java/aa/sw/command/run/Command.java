package aa.sw.command.run;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

@Value
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Command {

    Path workspace;
    Optional<String> workingDirectory;
    String commands;
    Map<String, String> environmentVariables;

    public static Command parse(final List<String> parameters) {
        requireNonNull(parameters);

        return builder()
                .commands(String.join("\n", parameters))
                .build();
    }

    public Command withWorkspace(final String workspace) {
        requireNonNull(workspace);

        return toBuilder()
                .workspace(Path.of(workspace))
                .build();
    }

    public Command withWorkingDirectory(final Optional<String> workingDirectory) {
        requireNonNull(workingDirectory);

        return toBuilder()
                .workingDirectory(workingDirectory)
                .build();
    }

    public Command withWorkingDirectory(String workingDirectory) {
        return withWorkingDirectory(Optional.ofNullable(workingDirectory));
    }

    public Command withEnvironmentVariables(final List<String> environmentVariables) {
        requireNonNull(environmentVariables);

        return toBuilder()
                .readEnvironmentVariables(environmentVariables)
                .build();
    }

    public Command withInterpolatedValues(final Optional<Map<String, String>> values) {
        requireNonNull(values);

        return values.map(this::withInterpolatedValues)
                .orElse(this);
    }

    public Command withInterpolatedValues(final Map<String, String> values) {
        requireNonNull(values);

        /* Nothing to interpolate */
        if (values.isEmpty()) {
            return this;
        }

        return toBuilder()
                .commands(withInterpolatedValues(commands, values))
                .environmentVariables(withInterpolatedValues(environmentVariables, values))
                .build();
    }

    private static String withInterpolatedValues(final String input, final Map<String, String> values) {
        requireNonNull(input);
        requireNonNull(values);

        final Pattern variablePattern = Pattern.compile("\\$\\{([A-Z0-9_-]+)}");

        String interpolated = input;

        final Matcher matcher = variablePattern.matcher(input);
        while (matcher.find()) {
            final String variable = matcher.group(1);
            final String value = values.get(variable);
            if (value != null) {
                interpolated = interpolated.replace("${" + variable + "}", value);
            }
        }

        return interpolated;
    }

    private static Map<String, String> withInterpolatedValues(final Map<String, String> input, final Map<String,
            String> values) {
        requireNonNull(input);
        requireNonNull(values);

        return input.entrySet().stream()
                .collect(Collectors.toUnmodifiableMap(
                        Map.Entry::getKey,
                        e -> values.getOrDefault(e.getKey(), e.getValue())
                ));
    }

    public File getExecutionDirectory() {
        return workingDirectory.map(workspace::resolve)
                .orElse(workspace)
                .toFile();
    }

    public String asFormattedString() {
        return formattedEnvironmentVariables()
                .concat(formattedCommand());
    }

    private String formattedWorkingDirectory() {
        return workingDirectory
                .map(w -> w.concat(" "))
                .orElse("");
    }

    private String formattedCommandPromptSymbol() {
        return "$ ";
    }

    private String formattedEnvironmentVariables() {
        return environmentVariables.
                entrySet()
                .stream()
                /* TODO: How should we handle sensitive values like passwords or keys? */
                .map(e -> String.format("%s='%s' ", e.getKey(), e.getValue()))
                .collect(Collectors.joining());
    }

    private String formattedCommand() {
        return commands;
    }

    public String toString() {
        return asFormattedString();
    }

    public static class CommandBuilder {

        public CommandBuilder readEnvironmentVariables(final List<String> environmentVariables) {
            requireNonNull(environmentVariables);

            final Collector<String, ?, LinkedHashMap<String, String>> collector =
                    Collectors.toMap(
                            Function.identity(),
                            this::readEnvironmentVariable,
                            (u, v) -> v, /* Reuse the existing value in case of duplicates */
                            LinkedHashMap::new
                    );

            final Map<String, String> read = environmentVariables.stream().collect(collector);
            return environmentVariables(read);
        }

        private String readEnvironmentVariable(final String variable) {
            return Stream.<Function<String, String>>of(System::getenv, System::getProperty)
                    .map(s -> s.apply(variable))
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse("");
        }

        public Command build() {
            return new Command(
                    defaultWorkspaceIfNull(workspace),
                    emptyIfNull(workingDirectory),
                    requireNonNull(commands),
                    unmodifiable(environmentVariables));
        }

        private static Path defaultWorkspaceIfNull(final Path workspace) {
            return Optional.ofNullable(workspace)
                    .orElseGet(CommandBuilder::defaultWorkspace);
        }

        private static Path defaultWorkspace() {
            return Path.of(System.getProperty("user.home"), "sociable-weaver", "workspace").toAbsolutePath();
        }

        private static <V> Optional<V> emptyIfNull(final Optional<V> source) {
            return Optional.ofNullable(source)
                    .orElse(Optional.empty());
        }

        private static <K, V> Map<K, V> unmodifiable(final Map<K, V> source) {
            return Optional.ofNullable(source)
                    /* Do not use Map.copyOf() as we need to preserve the order */
                    .map(Collections::unmodifiableMap)
                    .orElse(Collections.emptyMap());
        }
    }
}
