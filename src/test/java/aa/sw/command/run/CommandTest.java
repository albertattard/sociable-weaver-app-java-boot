package aa.sw.command.run;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class CommandTest {

    @Nested
    class ParseTest {

        @Test
        void handlesSingleLineCommandWithArguments() {
            /* Given */
            final List<String> parameters = List.of("java -jar hello-world.jar");

            /* When */
            final Command command = Command.parse(parameters);

            /* Then */
            final String commands = "java -jar hello-world.jar";
            assertThat(command)
                    .isEqualTo(create(commands));
        }

        @Test
        void handlesSingleQuotedArguments() {
            /* Given */
            final List<String> parameters = List.of("echo 'hello world'");

            /* When */
            final Command command = Command.parse(parameters);

            /* Then */
            final String commands = "echo 'hello world'";
            assertThat(command)
                    .isEqualTo(create(commands));
        }

        @Test
        void handlesSingleDoubleQuotedArguments() {
            /* Given */
            final List<String> parameters =
                    List.of("curl 'https://github.com/albertattard/' -H 'Accept: application/json'");

            /* When */
            final Command command = Command.parse(parameters);

            /* Then */
            final String commands = "curl 'https://github.com/albertattard/' -H 'Accept: application/json'";
            assertThat(command)
                    .isEqualTo(create(commands));
        }

        @Test
        void handlesDoubleQuotedArguments() {
            /* Given */
            final List<String> parameters = List.of("echo \"hello world\"");

            /* When */
            final Command command = Command.parse(parameters);

            /* Then */
            final String commands = "echo \"hello world\"";
            assertThat(command)
                    .isEqualTo(create(commands));
        }

        @Test
        void handlesMultipleDoubleQuotedArguments() {
            /* Given */
            final List<String> parameters =
                    List.of("curl \"https://github.com/albertattard/\" -H \"Accept: application/json\"");

            /* When */
            final Command command = Command.parse(parameters);

            /* Then */
            final String commands = "curl \"https://github.com/albertattard/\" -H \"Accept: application/json\"";
            assertThat(command)
                    .isEqualTo(create(commands));
        }

        @Test
        void handlesDoubleQuotesWithinSingleQuotedArguments() {
            /* Given */
            final List<String> parameters = List.of("echo '\"hello world\"'");

            /* When */
            final Command command = Command.parse(parameters);

            /* Then */
            final String commands = "echo '\"hello world\"'";
            assertThat(command)
                    .isEqualTo(create(commands));
        }

        @Test
        void handleMultiLineCommandWithArguments() {
            /* Given */
            final List<String> parameters = List.of(
                    "hub create \\",
                    "  --private \\",
                    "  --description 'Hello World Application (Java + Git + Gradle + Docker + GitHub Actions)' \\",
                    "  'programming--hello-world'"
            );

            /* When */
            final Command command = Command.parse(parameters);

            /* Then */
            final String expected = """
                    hub create \\
                      --private \\
                      --description 'Hello World Application (Java + Git + Gradle + Docker + GitHub Actions)' \\
                      'programming--hello-world'\
                    """;
            assertThat(command.getCommands())
                    .isEqualTo(expected);
        }
    }

    @Nested
    class InterpolateTest {

        @Test
        void interpolatesSingleVariableValue() {
            /* Given */
            final List<String> parameters = List.of("echo '${NAME}'");
            final Map<String, String> values = Map.of("NAME", "hello world");

            /* When */
            final Command command = Command.parse(parameters)
                    .withInterpolatedValues(values);

            /* Then */
            final String commands = "echo 'hello world'";
            assertThat(command)
                    .isEqualTo(create(commands));
        }

        @Test
        void interpolatesSingleVariableWithMultipleValues() {
            /* Given */
            final List<String> parameters = List.of("echo '${NAME} ${NAME}'");
            final Map<String, String> values = Map.of("NAME", "hello");

            /* When */
            final Command command = Command.parse(parameters)
                    .withInterpolatedValues(values);

            /* Then */
            final String commands = "echo 'hello hello'";
            assertThat(command)
                    .isEqualTo(create(commands));
        }

        @Test
        void interpolatesMultipleVariableValues() {
            /* Given */
            final List<String> parameters = List.of("echo '${NAME} from ${EMAIL}'");
            final Map<String, String> values = Map.of(
                    "NAME", "hello world",
                    "EMAIL", "someone@somewhere.com");

            /* When */
            final Command command = Command.parse(parameters)
                    .withInterpolatedValues(values);

            /* Then */
            final String commands = "echo 'hello world from someone@somewhere.com'";
            assertThat(command)
                    .isEqualTo(create(commands));
        }

        @Test
        void interpolatesEnvironmentVariableValues() {
            /* Given */
            final List<String> environmentVariables = List.of("NAME");
            final Map<String, String> values = Map.of("NAME", "hello world");

            /* When */
            final Command command = Command.parse(Collections.emptyList())
                    .withEnvironmentVariables(environmentVariables)
                    .withInterpolatedValues(values);

            /* Then */
            assertThat(command.getEnvironmentVariables())
                    .isEqualTo(Map.of("NAME", "hello world"));
        }
    }

    @Nested
    class ExecutionDirectoryTest {

        @Test
        void returnWorkspaceWhenWorkingDirectoryIsNotProvided() {
            /* Given */
            final Command command = Command.builder()
                    .commands("echo 'Hello world'")
                    .workspace(Path.of("workspace"))
                    .build();

            /* When */
            final File directory = command.getExecutionDirectory();

            /* Then */
            assertThat(directory)
                    .isEqualTo(new File("workspace"));
        }

        @Test
        void returnBothWorkspaceAndWorkingDirectory() {
            /* Given */
            final Command command = Command.builder()
                    .commands("echo 'Hello world'")
                    .workspace(Path.of("workspace"))
                    .workingDirectory(Optional.of("work-dir"))
                    .build();

            /* When */
            final File directory = command.getExecutionDirectory();

            /* Then */
            assertThat(directory)
                    .isEqualTo(new File("workspace", "work-dir"));
        }
    }

    @Nested
    class FormatCommandTest {

        @Test
        void formatSingleLineCommand() {
            /* Given */
            final Command command = Command.parse(List.of("echo 'hello world'"));

            /* When */
            final String formatted = command.asFormattedString();

            /* Then */
            assertThat(formatted)
                    .isEqualTo("$ echo 'hello world'");
        }

        @Test
        void formatMultiLineCommand() {
            /* Given */
            final Command command = Command.parse(List.of("echo \\", " 'hello world'"));

            /* When */
            final String formatted = command.asFormattedString();

            /* Then */
            assertThat(formatted)
                    .isEqualTo("$ echo \\\n 'hello world'");
        }

        @Test
        void formatCommandWithWorkingDirectory() {
            /* Given */
            final Command command = Command.parse(List.of("echo 'hello world'"))
                    .withWorkingDirectory("work-dir");

            /* When */
            final String formatted = command.asFormattedString();

            /* Then */
            assertThat(formatted)
                    .isEqualTo("work-dir $ echo 'hello world'");
        }

        @Test
        void formatCommandWithWorkingDirectoryAndEnvironmentVariables() {
            /* Given */
            final Command command = Command.parse(List.of("echo 'hello world'"))
                    .withWorkingDirectory("work-dir")
                    .withEnvironmentVariables(List.of("HELLO_WORLD", "WORLD_HELLO"));

            /* When */
            final String formatted = command.asFormattedString();

            /* Then */
            assertThat(formatted)
                    .isEqualTo("work-dir $ HELLO_WORLD='' WORLD_HELLO='' echo 'hello world'");
        }
    }

    private static Command create(final String commands) {
        return Command.builder()
                .commands(commands)
                .build();
    }
}
