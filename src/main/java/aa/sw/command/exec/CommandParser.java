package aa.sw.command.exec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CommandParser {

    public List<String> parse(final List<String> parameters) {

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

        return Collections.unmodifiableList(commandAndArgs);
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
