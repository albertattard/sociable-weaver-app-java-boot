package aa.sw.command.run;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Command {

    String formatted;
    List<String> commandAndArgs;

    public static Command parse(final List<String> parameters) {

        final List<String> commandAndArgs = new ArrayList<>();
        final StringBuilder buffer = new StringBuilder();
        boolean spacesAreIncludedInArg = false;
        Group group = null;

        for (String line : parameters) {
            for (char c : line.toCharArray()) {
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

        final String formatted = String.join("\n", parameters);

        return builder()
                .formatted(formatted)
                .commandAndArgs(commandAndArgs)
                .build();
    }

    public String toString() {
        return formatted;
    }

    public static class CommandBuilder {
        public Command build() {
            return new Command(formatted, List.copyOf(commandAndArgs));
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
