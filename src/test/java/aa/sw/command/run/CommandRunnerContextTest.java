package aa.sw.command.run;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

class CommandRunnerContextTest {

    @Nested
    class AppendErrorFormattedTest {

        @Test
        void printsStacktraceWhenLastParameterIsException() {
            /* Given */
            final String format = "Error message %s";
            final Object[] params = { "12345", new RuntimeException("The exception") };

            /* When */
            final BufferedOutputConsumer outputConsumer = new BufferedOutputConsumer();
            final CommandRunnerContext context = CommandRunnerContext.builder().output(outputConsumer).build();
            context.appendErrorF(format, params);

            /* Then */
            assertThat(outputConsumer.toString())
                    .contains("""
                            ================================================================================
                            Error message 12345 - java.lang.RuntimeException: The exception
                            --------------------------------------------------------------------------------
                            java.lang.RuntimeException: The exception
                            \tat aa.sw.command.run.CommandRunnerContextTest$AppendErrorFormattedTest.printsStacktraceWhenLastParameterIsException(CommandRunnerContextTest.java:19)
                            """);
        }

        private static class BufferedOutputConsumer implements Consumer<String> {

            private final StringBuilder buffer = new StringBuilder();

            @Override
            public void accept(final String output) {
                buffer.append(output);
            }

            @Override
            public String toString() {
                return buffer.toString();
            }
        }
    }
}
