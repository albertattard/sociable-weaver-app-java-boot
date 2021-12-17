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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

@Service
public class RunnableEntryRunner {

    private final Map<String, Function<RunnableEntry, RunnableEntryExecutionStrategy>> strategiesByName;

    public RunnableEntryRunner() {
        this(createDefaultStrategies());
    }

    RunnableEntryRunner(final Map<String, Function<RunnableEntry, RunnableEntryExecutionStrategy>> strategiesByName) {
        requireNonNull(strategiesByName);
        this.strategiesByName = Map.copyOf(strategiesByName);
    }

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

    private Optional<RunnableEntryExecutionStrategy> findStrategy(final RunnableEntry entry) {
        requireNonNull(entry);

        final String type = entry.getType().toLowerCase(Locale.ROOT);
        return Optional.ofNullable(strategiesByName.get(type))
                .map(factory -> factory.apply(entry));
    }

    private CommandResult execute(final RunnableEntryExecutionStrategy strategy, final Consumer<String> output) {
        requireNonNull(strategy);
        requireNonNull(output);

        final CommandRunnerContext context = CommandRunnerContext.builder()
                .output(output)
                .build();

        return strategy.execute(context);
    }

    private static Map<String, Function<RunnableEntry, RunnableEntryExecutionStrategy>> createDefaultStrategies() {
        final Map<String, Function<RunnableEntry, RunnableEntryExecutionStrategy>> map = new HashMap<>();
        map.put("command", CommandStrategy::of);
        map.put("create", CreateStrategy::of);
        map.put("docker-tag-and-push", DockerTagAndPushStrategy::of);
        map.put("download", DownloadStrategy::of);
        map.put("git-apply-patch", GitApplyPatchStrategy::of);
        map.put("git-commit-changes", GitCommitChangesStrategy::of);
        map.put("git-tag-current-commit", GitTagCurrentCommitStrategy::of);
        map.put("replace", ReplaceStrategy::of);
        return Map.copyOf(map);
    }
}
