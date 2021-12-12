package aa.sw.command.run;

import org.junit.jupiter.api.Test;

import java.util.List;

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

    private static Command create(final List<String> parameters, final List<String> commandAndArgs) {
        return Command.builder()
                .formatted(String.join("\n", parameters))
                .commandAndArgs(commandAndArgs)
                .build();
    }
}
