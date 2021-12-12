package aa.sw.command.exec;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CommandParserTest {

    private final CommandParser parser = new CommandParser();

    @Test
    void handlesSingleLineCommandWithArguments() {
        /* Given */
        final String input = "java -jar hello-world.jar";

        /* When */
        final List<String> commandAndArgs = parser.parse(List.of(input));

        /* Then */
        assertThat(commandAndArgs)
                .isEqualTo(List.of("java", "-jar", "hello-world.jar"));
    }

    @Test
    void handlesSingleQuotedArguments() {
        /* Given */
        final String input = "echo 'hello world'";

        /* When */
        final List<String> commandAndArgs = parser.parse(List.of(input));

        /* Then */
        assertThat(commandAndArgs)
                .isEqualTo(List.of("echo", "hello world"));
    }

    @Test
    void handlesSingleDoubleQuotedArguments() {
        /* Given */
        final String input = "curl 'https://github.com/albertattard/' -H 'Accept: application/json'";

        /* When */
        final List<String> commandAndArgs = parser.parse(List.of(input));

        /* Then */
        assertThat(commandAndArgs)
                .isEqualTo(List.of("curl", "https://github.com/albertattard/", "-H", "Accept: application/json"));
    }

    @Test
    void handlesDoubleQuotedArguments() {
        /* Given */
        final String input = "echo \"hello world\"";

        /* When */
        final List<String> commandAndArgs = parser.parse(List.of(input));

        /* Then */
        assertThat(commandAndArgs)
                .isEqualTo(List.of("echo", "hello world"));
    }

    @Test
    void handlesMultipleDoubleQuotedArguments() {
        /* Given */
        final String input = "curl \"https://github.com/albertattard/\" -H \"Accept: application/json\"";

        /* When */
        final List<String> commandAndArgs = parser.parse(List.of(input));

        /* Then */
        assertThat(commandAndArgs)
                .isEqualTo(List.of("curl", "https://github.com/albertattard/", "-H", "Accept: application/json"));
    }

    @Test
    void handlesDoubleQuotesWithinSingleQuotedArguments() {
        /* Given */
        final String input = "echo '\"hello world\"'";

        /* When */
        final List<String> commandAndArgs = parser.parse(List.of(input));

        /* Then */
        assertThat(commandAndArgs)
                .isEqualTo(List.of("echo", "\"hello world\""));
    }
}
