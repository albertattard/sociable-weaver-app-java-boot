package aa.sw.command.run.strategy;

import aa.sw.command.CommandResult;
import aa.sw.command.RunnableEntry;
import aa.sw.command.run.Command;
import aa.sw.command.run.CommandRunnerContext;
import aa.sw.command.run.RunnableEntryExecutionStrategy;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.Collections;
import java.util.List;

import static java.util.Objects.requireNonNull;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DownloadStrategy implements RunnableEntryExecutionStrategy {

    private final Command command;

    public static RunnableEntryExecutionStrategy of(final RunnableEntry entry) {
        requireNonNull(entry);

        final String link = entry.getParameterAtOrFail(0, "Missing download line");
        final String path = entry.getParameterAtOrFail(1, "Missing file path where the file will be saved");

        /* TODO: we need to ensure that the tags and the message do not escape, by having a single or double quote */
        final Command command = Command.parse(List.of(String.format("curl --location '%s' --output '%s'", link, path)))
                .withWorkspace(entry.getWorkspace())
                .withWorkingDirectory(entry.getWorkingDirectory())
                .withEnvironmentVariables(entry.getEnvironmentVariables().orElse(Collections.emptyList()))
                .withInterpolatedValues(entry.getValues());

        return new DownloadStrategy(command);
    }

    @Override
    public CommandResult execute(final CommandRunnerContext context) {
        return CommandStrategy.builder(command)
                .execute(context);
    }
}
