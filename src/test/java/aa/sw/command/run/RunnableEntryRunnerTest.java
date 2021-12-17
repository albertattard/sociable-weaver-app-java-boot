package aa.sw.command.run;

import aa.sw.command.CommandResult;
import aa.sw.command.RunnableEntry;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;

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
}
