package aa.sw.command.run;

import aa.sw.command.CommandResult;
import aa.sw.command.RunnableEntry;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RunnableEntryRunnerTest {

    @Test
    void returnDryRunIfTheEntryIsMarkedAsDryRun() {
        /* Given */
        final RunnableEntry entry = RunnableEntry.builder().dryRun(true).build();
        final RunnableEntryRunner runner = new RunnableEntryRunner();
        final StringWriter writer = new StringWriter();

        /* When */
        final CommandResult result = runner.run(entry, writer::write);

        /* Then */
        assertThat(result).isEqualTo(CommandResult.dryRun());
        assertThat(writer.toString()).isEqualTo("Cannot run an entry that is flagged as dry run!!");
    }

    @Test
    void returnExecutionStrategyNotFoundIfNoStrategyIsAvailableForEntry() {
        /* Given */
        final RunnableEntry entry = RunnableEntry.builder().type("something").build();
        final RunnableEntryRunner runner = new RunnableEntryRunner(Collections.emptyMap()); /* No strategies */
        final StringWriter writer = new StringWriter();

        /* When */
        final CommandResult result = runner.run(entry, writer::write);

        /* Then */
        assertThat(result).isEqualTo(CommandResult.executionStrategyNotFound());
        assertThat(writer.toString()).isEqualTo("No execution strategy found that can execute an entry of type something");
    }

    @Test
    void returnExecutionStrategyResultForTheMatchingEntry() {
        /* Given */
        final RunnableEntry entry = RunnableEntry.builder().type("something").build();
        final RunnableEntryExecutionStrategy strategy = mock(RunnableEntryExecutionStrategy.class);
        final RunnableEntryRunner runner = new RunnableEntryRunner(Map.of("something", (e) -> strategy));
        final StringWriter writer = new StringWriter();
        when(strategy.execute(any())).thenReturn(CommandResult.finishedAsExpected());

        /* When */
        final CommandResult result = runner.run(entry, writer::write);

        /* Then */
        assertThat(result).isEqualTo(CommandResult.finishedAsExpected());
    }
}
