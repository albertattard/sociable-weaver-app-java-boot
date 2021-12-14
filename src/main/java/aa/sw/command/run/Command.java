package aa.sw.command.run;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

@Value
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Command {

    Path workspace;
    Optional<String> workingDirectory;
    List<String> parameters;
    List<String> commandAndArgs;
    Map<String, String> environmentVariables;

    public static Command parse(final List<String> parameters) {
        requireNonNull(parameters);

        final List<String> commandAndArgs = new ArrayList<>();
        final StringBuilder buffer = new StringBuilder();
        boolean spacesAreIncludedInArg = false;
        Group group = null;

        for (final String line : parameters) {
            for (final char c : line.toCharArray()) {
                switch (c) {
                    case '\'':
                    case '"':
                        if (group == null) {
                            group = Group.of(c);
                            spacesAreIncludedInArg = true;
                        } else if (group.matches(c)) {
                            spacesAreIncludedInArg = false;
                            group = null;
                        } else {
                            buffer.append(c);
                        }
                        break;
                    case ' ':
                        if (spacesAreIncludedInArg) {
                            buffer.append(c);
                        } else {
                            commandAndArgs.add(buffer.toString());
                            buffer.setLength(0);
                        }
                        break;
                    default:
                        buffer.append(c);
                }
            }
        }

        /* Add the last argument */
        if (!buffer.isEmpty()) {
            commandAndArgs.add(buffer.toString());
        }

        return builder()
                .parameters(parameters)
                .commandAndArgs(commandAndArgs)
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
                .parameters(withInterpolatedValues(parameters, values))
                .commandAndArgs(withInterpolatedValues(commandAndArgs, values))
                .environmentVariables(withInterpolatedValues(environmentVariables, values))
                .build();
    }

    private static List<String> withInterpolatedValues(final List<String> input, final Map<String, String> values) {
        requireNonNull(input);
        requireNonNull(values);

        final Pattern variablePattern = Pattern.compile("\\$\\{(.+)}");

        final List<String> interpolated = new ArrayList<>(input.size());
        for (int i = 0, size = input.size(); i < size; i++) {
            final String text = input.get(i);
            interpolated.add(text);

            final Matcher matcher = variablePattern.matcher(text);
            if (matcher.find()) {
                final String variable = matcher.group(1);
                final String value = values.get(variable);
                if (value != null) {
                    interpolated.set(i, text.replace("${" + variable + "}", value));
                }
            }
        }

        return List.copyOf(interpolated);
    }

    private static Map<String, String> withInterpolatedValues(final Map<String, String> input, final Map<String,
            String> values) {
        requireNonNull(input);
        requireNonNull(values);

        return input.entrySet().stream()
                .collect(Collectors.toUnmodifiableMap(
                        Map.Entry::getKey,
                        (e) -> values.getOrDefault(e.getKey(), e.getValue())
                ));
    }

    public String asFormattedString() {
        return formattedWorkingDirectory()
                .concat(formattedCommandPromptSymbol())
                .concat(formattedEnvironmentVariables())
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
        return String.join("\n", parameters);
    }

    public String toString() {
        return asFormattedString();
    }

    public static class CommandBuilder {

        public CommandBuilder readEnvironmentVariables(final List<String> environmentVariables) {
            requireNonNull(environmentVariables);

            final Map<String, String> read = environmentVariables.stream()
                    .collect(Collectors.toUnmodifiableMap(variable -> variable, this::readEnvironmentVariable));
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
                    unmodifiable(parameters),
                    unmodifiable(commandAndArgs),
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

        private static <V> List<V> unmodifiable(final List<V> source) {
            return Optional.ofNullable(source)
                    .map(List::copyOf)
                    .orElse(Collections.emptyList());
        }

        private static <K, V> Map<K, V> unmodifiable(final Map<K, V> source) {
            return Optional.ofNullable(source)
                    .map(Map::copyOf)
                    .orElse(Collections.emptyMap());
        }
    }

    private enum Group {
        SINGLE('\''),
        DOUBLE('"');

        private final char c;

        Group(char c) { this.c = c; }

        public static Group of(char c) {
            return Arrays.stream(values())
                    .filter(g -> g.c == c)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Group termination symbol not found"));
        }

        private boolean matches(final char c) {
            return this.c == c;
        }
    }
}
