package aa.sw.command.run.strategy;

import aa.sw.command.CommandResult;
import aa.sw.command.RunnableEntry;
import aa.sw.command.run.CommandRunnerContext;
import aa.sw.command.run.RunnableEntryExecutionStrategy;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ReplaceStrategy implements RunnableEntryExecutionStrategy {

    private final String workspace;
    private final String workingDirectory;
    private final String destination;
    private final String content;

    public static RunnableEntryExecutionStrategy of(final RunnableEntry entry) {
        requireNonNull(entry);

        final String workspace = entry.getWorkPath();
        final String workingDirectory = entry.getWorkingDirectory().orElse(".");
        final String destination = entry.getParameterAtOrFail(0, "Missing destination file path");
        final String content = entry.getParametersAsStream().skip(1).collect(Collectors.joining("\n"));

        return new ReplaceStrategy(workspace, workingDirectory, destination, content);
    }

    @Override
    public CommandResult execute(final CommandRunnerContext context) {
        requireNonNull(context);

        final String baseDirectory = new File(workspace).getAbsolutePath();
        final Path destinationPath = Paths.get(baseDirectory, workingDirectory, this.destination);

        try {
            Files.writeString(destinationPath, content, StandardCharsets.UTF_8);
            context.appendLineF("File %s updated", this.destination);
        } catch (final IOException e) {
            context.appendErrorF("Failed to update file %s", destinationPath, e);
            return CommandResult.finishedNotAsExpected();
        }

        return CommandResult.finishedAsExpected();
    }
}
