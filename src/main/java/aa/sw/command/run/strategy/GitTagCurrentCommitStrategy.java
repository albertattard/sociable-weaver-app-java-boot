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
import java.util.Optional;

import static java.util.Objects.requireNonNull;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GitTagCurrentCommitStrategy implements RunnableEntryExecutionStrategy {

    private final Command command;

    public static RunnableEntryExecutionStrategy of(final RunnableEntry entry) {
        requireNonNull(entry);

        final String tag = entry.getParameterAtOrFail(0, "Missing tag");
        final Optional<String> message = entry.getParameterAt(1);

        /* TODO: we need to ensure that the tags and the message do not escape, by having a single or double quote */
        final String parameters = message
                .map(m -> String.format("git tag --annotate '%s' --message '%s'", tag, m))
                .orElse(String.format("git tag --annotate '%s'", tag));

        final Command command = Command.parse(List.of(parameters))
                .withWorkspace(entry.getWorkPath())
                .withWorkingDirectory(entry.getWorkingDirectory())
                .withEnvironmentVariables(entry.getEnvironmentVariables().orElse(Collections.emptyList()))
                .withInterpolatedValues(entry.getValues());

        return new GitTagCurrentCommitStrategy(command);
    }

    @Override
    public CommandResult execute(final CommandRunnerContext context) {
        return CommandStrategy.builder(command)
                .execute(context);
    }
}
