package aa.sw.command;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class RunnableEntryTest {

    @Nested
    class ParametersAsStreamTest {

        @Test
        void returnAnEmptyStreamWhenNoParametersAreProvided() {
            /* Given */
            final RunnableEntry entry = RunnableEntry.builder().build();

            /* When */
            final Stream<String> stream = entry.getParametersAsStream();

            /* Then */
            assertEquals(0, stream.count());
        }

        @Test
        void returnAStreamWithAllProvidedParameters() {
            /* Given */
            final List<String> parameters = List.of("a", "b", "c");
            final RunnableEntry entry = RunnableEntry.builder()
                    .parameters(parameters)
                    .build();

            /* When */
            final Stream<String> stream = entry.getParametersAsStream();

            /* Then */
            assertEquals(parameters, stream.toList());
        }

        @Test
        void returnJoinedParameters() {
            /* Given */
            final List<String> parameters = List.of("a", "b", "c", "");
            final RunnableEntry entry = RunnableEntry.builder()
                    .parameters(parameters)
                    .build();

            /* When */
            final String text = entry.joinParametersWithOrFail("\n");

            /* Then */
            assertEquals("a\nb\nc\n", text);
        }
    }
}
