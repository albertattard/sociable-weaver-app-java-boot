package aa.sw.command.run.strategy;

import aa.sw.command.CommandResult;
import aa.sw.command.RunnableEntry;
import aa.sw.command.run.Command;
import aa.sw.command.run.CommandRunnerContext;
import aa.sw.command.run.RunnableEntryExecutionStrategy;
import com.google.common.hash.Hashing;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GitApplyPatchStrategy implements RunnableEntryExecutionStrategy {

    private final String workspace;
    private final Optional<String> workingDirectory;
    private final String patch;

    public static RunnableEntryExecutionStrategy of(final RunnableEntry entry) {
        requireNonNull(entry);

        final String workspace = entry.getWorkPath();
        final Optional<String> workingDirectory = entry.getWorkingDirectory();
        final String patch = entry.joinParametersWithOrFail("\n");

        return new GitApplyPatchStrategy(workspace, workingDirectory, patch);
    }

    @Override
    public CommandResult execute(final CommandRunnerContext context) {
        final File patchesFolder = new File(workspace, ".patches").getAbsoluteFile();
        if (!patchesFolder.exists() && !patchesFolder.mkdirs()) {
            context.appendError("Failed to create the folder were to temporary save the patch");
            return CommandResult.finishedNotAsExpected();
        }

        if (!patchesFolder.isDirectory()) {
            context.appendError("The temporary patches folder does not exists and failed to be created silently");
            return CommandResult.finishedNotAsExpected();
        }

        final String fileName = getPatchFileName();
        final File patchFile = new File(patchesFolder, fileName);
        if (!writeUtf8ToFile(patch, patchFile, context)) {
            context.appendError("Failed to save the patch into the temporary file");
            return CommandResult.finishedNotAsExpected();
        }

        final Command command =
                Command.parse(List.of(String.format("git apply '%s'", patchFile.getAbsolutePath())))
                        .withWorkspace(workspace)
                        .withWorkingDirectory(workingDirectory);

        return CommandStrategy.builder(command).execute(context);
    }

    private String getPatchFileName() {
        return Hashing.sha256()
                .hashString(patch, StandardCharsets.UTF_8)
                .toString()
                .concat(".patch");
    }


    private static boolean writeUtf8ToFile(final String text, final File file, final CommandRunnerContext context) {
        requireNonNull(text);
        requireNonNull(file);

        try {
            Files.writeString(file.toPath(), text, StandardCharsets.UTF_8);
            return true;
        } catch (final IOException e) {
            context.appendError("Failed to write file " + file, e);
            return false;
        }
    }
}
