package aa.sw.command.run;

import aa.sw.command.CommandResult;
import aa.sw.command.RunnableEntry;
import aa.sw.command.run.strategy.CommandStrategy;
import aa.sw.command.run.strategy.CreateStrategy;
import aa.sw.command.run.strategy.DockerTagAndPushStrategy;
import aa.sw.command.run.strategy.DownloadStrategy;
import aa.sw.command.run.strategy.GitApplyPatchStrategy;
import aa.sw.command.run.strategy.GitCommitChangesStrategy;
import aa.sw.command.run.strategy.GitTagCurrentCommitStrategy;
import aa.sw.command.run.strategy.ReplaceStrategy;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

@Service
public class RunnableEntryRunner {

    public CommandResult run(final RunnableEntry entry, final Consumer<String> output) {
        requireNonNull(entry);
        requireNonNull(output);

        if (entry.isDryRun()) {
            output.accept("Cannot run an entry that is flagged as dry run!!");
            return CommandResult.dryRun();
        }

        return findStrategy(entry)
                .map(strategy -> execute(strategy, output))
                .orElseGet(() -> {
                    output.accept("No execution strategy found that can execute an entry of type " + entry.getType());
                    return CommandResult.executionStrategyNotFound();
                });
    }

    private CommandResult execute(final RunnableEntryExecutionStrategy strategy, final Consumer<String> output) {
        requireNonNull(strategy);
        requireNonNull(output);

        final CommandRunnerContext context = CommandRunnerContext.builder()
                .output(output)
                .build();

        return strategy.execute(context);
    }

    private Optional<RunnableEntryExecutionStrategy> findStrategy(final RunnableEntry entry) {
        requireNonNull(entry);

        return switch (entry.getType().toLowerCase(Locale.ROOT)) {
            case "command" -> Optional.of(CommandStrategy.of(entry));
            case "create" -> Optional.of(CreateStrategy.of(entry));
            case "docker-tag-and-push" -> Optional.of(DockerTagAndPushStrategy.of(entry));
            case "download" -> Optional.of(DownloadStrategy.of(entry));
            case "git-apply-patch" -> Optional.of(GitApplyPatchStrategy.of(entry));
            case "git-commit-changes" -> Optional.of(GitCommitChangesStrategy.of(entry));
            case "git-tag-current-commit" -> Optional.of(GitTagCurrentCommitStrategy.of(entry));
            case "replace" -> Optional.of(ReplaceStrategy.of(entry));
            default -> Optional.empty();
        };
    }
}
