package aa.sw.book;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.Collections;
import java.util.List;

import static java.util.Objects.requireNonNull;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MultipartParameters {

    private final List<String> parameters;

    public static MultipartParameters of(final List<String> parameters) {
        requireNonNull(parameters);

        return new MultipartParameters(List.copyOf(parameters));
    }

    public List<String> getPart(final String name) {
        final IndexAndLength indexAndLength = findPart(name);
        if (indexAndLength.index != -1) {
            final int contentIndex = indexAndLength.index + 1;
            return parameters.subList(contentIndex, contentIndex + indexAndLength.length);
        }

        return Collections.emptyList();
    }

    private IndexAndLength findPart(final String name) {
        int i = 0;

        while (i < parameters.size()) {
            final String header = parameters.get(i);
            final String[] parts = header.split(":", 2);
            final int length = Integer.parseInt(parts[1]);

            if (name.equals(parts[0])) {
                return new IndexAndLength(i, length);
            }

            i += length + 1;
        }

        return new IndexAndLength(-1, -1);
    }

    private static record IndexAndLength(int index, int length) {}
}
