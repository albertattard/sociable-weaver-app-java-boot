package aa.sw.command.run.strategy;

import aa.sw.command.CommandResult;
import aa.sw.command.RunnableEntry;
import aa.sw.command.run.Command;
import aa.sw.command.run.CommandRunnerContext;
import aa.sw.command.run.RunnableEntryExecutionStrategy;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.List;

import static java.util.Objects.requireNonNull;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DownloadStrategy implements RunnableEntryExecutionStrategy {

    private final Command command;

    public static RunnableEntryExecutionStrategy of(final RunnableEntry entry) {
        requireNonNull(entry);

        final String link = entry.getParameterAtOrFail(0, "Missing download line");
        final String path = entry.getParameterAtOrFail(1, "Missing file path where the file will be saved");

        final Command command = Command.parse(List.of(String.format("curl --location '%s' --output '%s'", link, path)))
                .withInterpolatedValues(entry.getValues())
                .withWorkspace(entry.getWorkspace())
                .withWorkingDirectory(entry.getWorkingDirectory());

        return new DownloadStrategy(command);
    }

    @Override
    public CommandResult execute(final CommandRunnerContext context) {
        return CommandStrategy.builder(command)
                .execute(context);
    }
}
