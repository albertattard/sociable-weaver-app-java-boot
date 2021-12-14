package aa.sw.command.run;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CommandTest {

    @Test
    void handlesSingleLineCommandWithArguments() {
        /* Given */
        final List<String> parameters = List.of("java -jar hello-world.jar");

        /* When */
        final Command command = Command.parse(parameters);

        /* Then */
        final List<String> commandAndArgs = List.of("java", "-jar", "hello-world.jar");
        assertThat(command)
                .isEqualTo(create(parameters, commandAndArgs));
    }

    @Test
    void handlesSingleQuotedArguments() {
        /* Given */
        final List<String> parameters = List.of("echo 'hello world'");

        /* When */
        final Command command = Command.parse(parameters);

        /* Then */
        final List<String> commandAndArgs = List.of("echo", "hello world");
        assertThat(command)
                .isEqualTo(create(parameters, commandAndArgs));
    }

    @Test
    void handlesSingleDoubleQuotedArguments() {
        /* Given */
        final List<String> parameters =
                List.of("curl 'https://github.com/albertattard/' -H 'Accept: application/json'");

        /* When */
        final Command command = Command.parse(parameters);

        /* Then */
        final List<String> commandAndArgs =
                List.of("curl", "https://github.com/albertattard/", "-H", "Accept: application/json");
        System.out.println(command.getCommandAndArgs());
        assertThat(command)
                .isEqualTo(create(parameters, commandAndArgs));
    }

    @Test
    void handlesDoubleQuotedArguments() {
        /* Given */
        final List<String> parameters = List.of("echo \"hello world\"");

        /* When */
        final Command command = Command.parse(parameters);

        /* Then */
        final List<String> commandAndArgs = List.of("echo", "hello world");
        assertThat(command)
                .isEqualTo(create(parameters, commandAndArgs));
    }

    @Test
    void handlesMultipleDoubleQuotedArguments() {
        /* Given */
        final List<String> parameters =
                List.of("curl \"https://github.com/albertattard/\" -H \"Accept: application/json\"");

        /* When */
        final Command command = Command.parse(parameters);

        /* Then */
        final List<String> commandAndArgs =
                List.of("curl", "https://github.com/albertattard/", "-H", "Accept: application/json");
        assertThat(command)
                .isEqualTo(create(parameters, commandAndArgs));
    }

    @Test
    void handlesDoubleQuotesWithinSingleQuotedArguments() {
        /* Given */
        final List<String> parameters = List.of("echo '\"hello world\"'");

        /* When */
        final Command command = Command.parse(parameters);

        /* Then */
        final List<String> commandAndArgs = List.of("echo", "\"hello world\"");
        assertThat(command)
                .isEqualTo(create(parameters, commandAndArgs));
    }

    @Nested
    public class InterpolateTest {

        @Test
        void interpolatesVariableValues() {
            /* Given */
            final List<String> parameters = List.of("echo '${NAME}'");
            final Map<String, String> values = Map.of("NAME", "hello world");

            /* When */
            final Command command = Command.parse(parameters)
                    .withInterpolatedValues(values);

            /* Then */
            final List<String> commandAndArgs = List.of("echo", "hello world");
            assertThat(command)
                    .isEqualTo(create(List.of("echo 'hello world'"), commandAndArgs));
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
    public class FormatCommandTest {

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

    private static Command create(final List<String> parameters, final List<String> commandAndArgs) {
        return Command.builder()
                .parameters(parameters)
                .commandAndArgs(commandAndArgs)
                .build();
    }
}
