package aa.sw.command.run;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

@Value
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Command {

    Path workspace;
    Optional<String> workingDirectory;
    List<String> parameters;
    List<String> commandAndArgs;

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

    public Command interpolate(final Optional<Map<String, String>> values) {
        requireNonNull(values);

        return values.map(this::interpolate)
                .orElse(this);
    }

    public Command interpolate(final Map<String, String> values) {
        requireNonNull(values);

        return toBuilder()
                .parameters(interpolate(parameters, values))
                .commandAndArgs(interpolate(commandAndArgs, values))
                .build();
    }

    private static List<String> interpolate(final List<String> input, final Map<String, String> variables) {
        requireNonNull(input);
        requireNonNull(variables);

        final Pattern variablePattern = Pattern.compile("\\$\\{(.+)}");

        final List<String> interpolated = new ArrayList<>(input.size());
        for (int i = 0, size = input.size(); i < size; i++) {
            final String text = input.get(i);
            interpolated.add(text);

            final Matcher matcher = variablePattern.matcher(text);
            if (matcher.find()) {
                final String variable = matcher.group(1);
                final String value = variables.get(variable);
                if (value != null) {
                    interpolated.set(i, text.replace("${" + variable + "}", value));
                }
            }
        }

        return interpolated;
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

    public String asString() {
        return String.join("\n", parameters);
    }

    public String toString() {
        return asString();
    }

    public static class CommandBuilder {
        private Path workspace = userHomeDirectory();

        public Command build() {
            return new Command(workspace, workingDirectory, List.copyOf(parameters), List.copyOf(commandAndArgs));
        }

        private static Path userHomeDirectory() {
            return Path.of(System.getProperty("user.home"), "sociable-weaver/workspace");
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
