package aa.sw.command.run;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;

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

import static java.util.Objects.requireNonNull;

@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ProcessRunner {

    private final List<String> command;
    private final File executionDirectory;
    private final CommandRunnerContext context;
    private final Map<String, String> environmentVariables;
    private final Duration commandTimeout;

    public ProcessResult run() {
        if (!executionDirectory.exists() && !executionDirectory.mkdirs()) {
            context.appendErrorF("Failed to create working directory: %s", executionDirectory);
            return ProcessResult.notStarted();
        }

        final ProcessBuilder builder = new ProcessBuilder(command);
        builder.directory(executionDirectory);
        builder.environment().putAll(environmentVariables);
        builder.redirectErrorStream(true);

        return start(builder)
                .map(this::handleProcess)
                .orElse(ProcessResult.notStarted());
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

    public static class ProcessRunnerBuilder {
        public ProcessRunner build() {
            return new ProcessRunner(
                    requireNonNull(command),
                    defaultDirectoryIfNull(executionDirectory),
                    requireNonNull(context),
                    emptyIfNull(environmentVariables),
                    defaultTimeoutIfNull(commandTimeout)
            );
        }

        private static File defaultDirectoryIfNull(final File source) {
            return Optional.ofNullable(source)
                    .orElse(defaultDirectory());
        }

        private static File defaultDirectory() {
            return new File(System.getProperty("user.home"), "sociable-weaver/workspace").getAbsoluteFile();
        }

        private static <K, V> Map<K, V> emptyIfNull(final Map<K, V> source) {
            return Optional.ofNullable(source)
                    .orElse(Collections.emptyMap());
        }

        private static Duration defaultTimeoutIfNull(final Duration commandTimeout) {
            return Optional.ofNullable(commandTimeout)
                    .orElse(Duration.ofSeconds(5));
        }
    }
}
