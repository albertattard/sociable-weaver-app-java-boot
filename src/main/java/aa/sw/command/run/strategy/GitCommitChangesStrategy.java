package aa.sw.command.run.strategy;

import aa.sw.command.CommandResult;
import aa.sw.command.RunnableEntry;
import aa.sw.command.run.Command;
import aa.sw.command.run.CommandRunnerContext;
import aa.sw.command.run.RunnableEntryExecutionStrategy;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GitCommitChangesStrategy implements RunnableEntryExecutionStrategy {

    private final String workspace;
    private final Optional<String> workingDirectory;
    private final String message;
    private final Optional<String> tag;
    private final boolean pushChanges;

    public static RunnableEntryExecutionStrategy of(final RunnableEntry entry) {
        requireNonNull(entry);

        final String workspace = entry.getWorkPath();
        final Optional<String> workingDirectory = entry.getWorkingDirectory();
        final String message = entry.getParameterAtOrFail(0, "Missing commit message");
        final Optional<String> tag = entry.getParameterAt(1);
        final boolean pushChanges = entry.getPushChanges().orElse(false);

        return new GitCommitChangesStrategy(workspace, workingDirectory, message, tag, pushChanges);
    }

    @Override
    public CommandResult execute(final CommandRunnerContext context) {
        return add(context)
                .then(() -> commit(context))
                .thenIfPresent(tag, t -> tag(context, t))
                .thenIfTrue(pushChanges, () -> push(context));
    }

    private CommandResult add(final CommandRunnerContext context) {
        return executeCommand(context, "git add .");
    }

    private CommandResult push(final CommandRunnerContext context) {
        final String push =
                tag.map(t -> String.format("git push --atomic origin main '%s'", t))
                        .orElse("git push origin main");
        return executeCommand(context, push);
    }

    private CommandResult tag(final CommandRunnerContext context, final String tag) {
        return executeCommand(context, String.format("git tag --annotate '%s' --message '%s'", tag, message));
    }

    private CommandResult commit(final CommandRunnerContext context) {
        return executeCommand(context, String.format("git commit --message '%s'", message));
    }

    private CommandResult executeCommand(final CommandRunnerContext context, final String command) {
        final Command c = Command.parse(List.of(command))
                .withWorkspace(workspace)
                .withWorkingDirectory(workingDirectory);

        return CommandStrategy.builder(c)
                .execute(context);
    }
}
