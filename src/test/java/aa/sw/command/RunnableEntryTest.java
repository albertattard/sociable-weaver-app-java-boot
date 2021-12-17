package aa.sw.command;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
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

    @Nested
    class OptionalValuesTest {
        @Test
        void returnEmptyOptionalWhenValuesAreNotSet() {
            /* Given */
            final RunnableEntry entry = RunnableEntry.builder().build();

            /* When/Then */
            assertThat(entry.getName()).isEmpty();
            assertThat(entry.getWorkingDirectory()).isEmpty();
            assertThat(entry.getParameters()).isEmpty();
            assertThat(entry.getVariables()).isEmpty();
            assertThat(entry.getEnvironmentVariables()).isEmpty();
            assertThat(entry.getValues()).isEmpty();
            assertThat(entry.getIgnoreErrors()).isEmpty();
            assertThat(entry.getPushChanges()).isEmpty();
            assertThat(entry.getDryRun()).isEmpty();
            assertThat(entry.getExpectedExitValue()).isEmpty();
            assertThat(entry.getCommandTimeout()).isEmpty();
        }
    }
}
