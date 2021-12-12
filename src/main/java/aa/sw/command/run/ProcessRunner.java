package aa.sw.command.run;

import lombok.Builder;
import lombok.NonNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Builder
public class ProcessRunner {

    @NonNull
    private final List<String> command;
    @Builder.Default
    private final Optional<String> workingDirectory = Optional.empty();
    @NonNull
    private final CommandRunnerContext context;
    @Builder.Default
    private final Map<String, String> environmentVariables = Collections.emptyMap();
    @Builder.Default
    private final Duration commandTimeout = Duration.ofSeconds(5);

    public ProcessResult run() {
        final File directory = getProcessDirectory();
        if (!directory.exists() && !directory.mkdirs()) {
            context.appendErrorF("Failed to create working directory: %s", directory);
            return ProcessResult.notStarted();
        }

        final ProcessBuilder builder = new ProcessBuilder(command);
        builder.directory(directory);
        builder.environment().putAll(environmentVariables);
        builder.redirectErrorStream(true);

        return start(builder)
                .map(this::handleProcess)
                .orElse(ProcessResult.notStarted());
    }

    private File getProcessDirectory() {
        return workingDirectory.map(name -> new File(context.baseDirectory(), name))
                .orElse(context.baseDirectory());
    }

    private Optional<Process> start(final ProcessBuilder builder) {
        try {
            return Optional.of(builder.start());
        } catch (final IOException e) {
            context.appendError("Failed to run process", e);
            return Optional.empty();
        }
    }

    private ProcessResult handleProcess(final Process process) {
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        final Future<?> output = executor.submit(() -> readOutput(process));

        final boolean finishedInTime = waitFor(process);

        waitExecutorToExit(executor, output);

        if (finishedInTime) {
            final int exitValue = process.exitValue();
            return ProcessResult.finishedWithExitValue(exitValue);
        }

        process.destroyForcibly();
        context.appendError("Process timed out and was killed");
        return ProcessResult.timedOut();
    }

    private void readOutput(final Process process) {
        try (BufferedReader reader = getReader(process)) {
            for (String line; (line = reader.readLine()) != null; ) {
                context.appendLine(line);

                if (Thread.currentThread().isInterrupted()) {
                    context.appendLine(line);
                    return;
                }
            }
        } catch (final IOException e) {
            context.appendError("Failed to capture process output", e);
        }
    }

    private static BufferedReader getReader(final Process process) {
        return new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
    }

    private boolean waitFor(final Process process) {
        try {
            return process.waitFor(commandTimeout.getSeconds(), TimeUnit.SECONDS);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            context.appendError("Interrupted while waiting for the process to finish", e);
            return false;
        }
    }

    private void waitExecutorToExit(final ExecutorService executor, final Future<?> output) {
        try {
            executor.shutdown();

            /* TODO: We have already waited for the process to finish, thus there is no need to wait long for the output
                consumer to take much longer */
            final boolean finishedInTime = executor.awaitTermination(2, TimeUnit.SECONDS);
            if (!finishedInTime) {
                output.cancel(true);
            }
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            context.appendError("Interrupted while waiting for the output executor to finish", e);
        }
    }
}
