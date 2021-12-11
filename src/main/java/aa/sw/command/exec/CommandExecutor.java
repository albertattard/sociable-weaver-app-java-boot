package aa.sw.command.exec;

import aa.sw.command.CommandResult;
import aa.sw.command.RunnableEntry;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

@Service
public class CommandExecutor {

    public CommandResult run(final RunnableEntry entry, final Consumer<String> output) {
        requireNonNull(entry);
        requireNonNull(output);

        if (entry.isDryRun()) {
            output.accept("Cannot run as entry is marked as dry run!!");
            return CommandResult.dryRun();
        }

        return findExecutor(entry)
                .map(strategy -> execute(strategy, output))
                .orElseGet(() -> {
                    output.accept("No execution strategy found that can execute this entry");
                    return CommandResult.executionStrategyNotFound();
                });
    }


    private CommandResult execute(final CodeExecutorStrategy strategy, final Consumer<String> output) {
        requireNonNull(strategy);
        requireNonNull(output);

        final CommandRunnerContext context = CommandRunnerContext.builder()
                // TODO: read this from the RunnableEntry
                // .baseDirectory(properties.getBaseDirectory())
                .baseDirectory(new File("/Users/albertattard/Projects/workspace"))
                .output(output)
                .build();

        return strategy.execute(context);
    }

    private Optional<CodeExecutorStrategy> findExecutor(final RunnableEntry entry) {
        requireNonNull(entry);

        return switch (entry.getType().toLowerCase(Locale.ROOT)) {
            case "command" -> Optional.of(Command.of(entry));
//            case "create" -> Optional.of(Create.of(entry));
//            case "docker-tag-and-push" -> Optional.of(DockerTagAndPush.of(entry));
//            case "download" -> Optional.of(Download.of(entry));
//            case "git-apply-patch" -> Optional.of(GitApplyPatch.of(entry));
//            case "git-commit-changes" -> Optional.of(GitCommitChanges.of(entry));
//            case "git-tag-current-commit" -> Optional.of(GitTagCurrentCommit.of(entry));
//            case "replace" -> Optional.of(Replace.of(entry));
            default -> Optional.empty();
        };
    }
}
