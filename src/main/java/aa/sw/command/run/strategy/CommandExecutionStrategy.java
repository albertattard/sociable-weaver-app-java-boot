package aa.sw.command.run.strategy;

import aa.sw.command.CommandResult;
import aa.sw.command.RunnableEntry;
import aa.sw.command.run.Command;
import aa.sw.command.run.CommandFormatter;
import aa.sw.command.run.CommandRunnerContext;
import aa.sw.command.run.ProcessResult;
import aa.sw.command.run.ProcessRunner;
import aa.sw.command.run.RunnableEntryExecutorStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public class CommandExecutionStrategy implements RunnableEntryExecutorStrategy {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandExecutionStrategy.class);

    private final Optional<String> workingDirectory;
    private final List<String> commandAndArgs;
    private final List<String> environmentVariables;
    private final boolean ignoreErrors;
    private final int expectedExitValue;
    private final Duration commandTimeout;

    public static RunnableEntryExecutorStrategy of(final RunnableEntry entry) {
        requireNonNull(entry);

        final List<String> parameters = entry.getParameters()
                .orElseThrow(() -> new IllegalArgumentException("Missing command"));

        final Command parser = Command.parse(parameters)
                .interpolate(entry.getValues());

        return new Builder(parser.getCommandAndArgs())
                .workingDirectory(entry.getWorkingDirectory())
                .environmentVariables(entry.getEnvironmentVariables().orElse(Collections.emptyList()))
                .ignoreErrors(entry.getIgnoreErrors().orElse(false))
                .expectedExitValue(entry.getExpectedExitValue().orElse(0))
                .commandTimeout(entry.getCommandTimeout().orElse(Duration.ofMinutes(5)))
                .build();
    }

    private CommandExecutionStrategy(final Builder builder) {
        requireNonNull(builder);

        this.workingDirectory = builder.workingDirectory;
        this.commandAndArgs = builder.commandAndArgs;
        this.environmentVariables = builder.environmentVariables;
        this.ignoreErrors = builder.ignoreErrors;
        this.expectedExitValue = builder.expectedExitValue;
        this.commandTimeout = builder.commandTimeout;
    }

    public static CommandResult execute(final CommandRunnerContext context, final Optional<String> workingDirectory,
                                        final String... command) {
        return execute(context, workingDirectory, List.of(command));
    }

    public static CommandResult execute(final CommandRunnerContext context, final Optional<String> workingDirectory,
                                        final List<String> command) {
        return new Builder(command)
                .workingDirectory(workingDirectory)
                .build()
                .execute(context);
    }

    public static CommandResult execute(final CommandRunnerContext context, final Optional<String> workingDirectory,
                                        final List<String> environmentVariables,
                                        final String... command) {
        return execute(context, workingDirectory, environmentVariables, List.of(command));
    }

    public static CommandResult execute(final CommandRunnerContext context, final Optional<String> workingDirectory,
                                        final List<String> environmentVariables,
                                        final List<String> command) {
        return new Builder(command)
                .workingDirectory(workingDirectory)
                .environmentVariables(environmentVariables)
                .build()
                .execute(context);
    }

    @Override
    public CommandResult execute(final CommandRunnerContext context) {
        context.appendLine(CommandFormatter.format(workingDirectory, commandAndArgs));

        return ProcessRunner.builder()
                .context(context)
                .command(commandAndArgs)
                .workingDirectory(workingDirectory)
                /* TODO: we need to retrieve these from somewhere */
                .environmentVariables(environmentVariables.stream().collect(Collectors.toMap(k -> k, v -> v)))
                .commandTimeout(commandTimeout)
                .build()
                .run()
                .map(this::determineResult);
    }

    private CommandResult determineResult(final ProcessResult result) {
        return switch (result.getExitState()) {
            case NOT_STARTED -> CommandResult.failedToStart();
            case TIMED_OUT -> CommandResult.timedOut();
            case FINISHED_IN_TIME -> result.getExitValue()
                    .stream()
                    .peek(this::logExitValue)
                    .filter(v -> v == expectedExitValue)
                    .mapToObj(v -> CommandResult.finishedAsExpected())
                    .findFirst()
                    .orElse(ignoreErrors
                            ? CommandResult.finishedWithSuppressedError()
                            : CommandResult.finishedNotAsExpected());
        };
    }

    private void logExitValue(final int exitValue) {
        LOGGER.debug("Command {} finished with exit value {} (expecting {})",
                CommandFormatter.format(workingDirectory, commandAndArgs), exitValue, expectedExitValue);
    }

    private void waitForTheProcessToFinish(final Process process) {
        final boolean finished;
        try {
            final long waitTimeInSeconds = Math.max(1, commandTimeout.getSeconds());
            finished = process.waitFor(waitTimeInSeconds, TimeUnit.SECONDS);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for the command to complete", e);
        }

        if (finished) {
            final int exitValue = process.exitValue();
            assertExitValue(exitValue);
            return;
        }

        LOGGER.warn("Command took longer than expected. Killing it!");
        process.destroyForcibly();
        throw new RuntimeException("Command took longer than expected and was killed");
    }

    private void assertExitValue(final int exitValue) {
        if (exitValue == expectedExitValue) {
            LOGGER.debug("Command finished with expected exit value {}", exitValue);
        } else if (ignoreErrors) {
            LOGGER.warn("Command finished with exit value {} while it was expected to finish with {}",
                    exitValue, expectedExitValue);
        } else {
            throw new RuntimeException(
                    "Command finished with exit value " + exitValue +
                            " while it was expected to finish with " + expectedExitValue);
        }
    }

    public static Builder builder(final String... command) {
        return builder(List.of(command));
    }

    public static Builder builder(final List<String> command) {
        return new Builder(command);
    }

    public static class Builder {
        private final List<String> commandAndArgs;
        private Optional<String> workingDirectory = Optional.empty();
        private List<String> environmentVariables = Collections.emptyList();
        private boolean ignoreErrors;
        private int expectedExitValue = 0;
        private Duration commandTimeout = Duration.ofMinutes(5);

        private Builder(final List<String> command) {
            this.commandAndArgs = requireNonNull(command);
        }

        public Builder workingDirectory(final Optional<String> workingDirectory) {
            this.workingDirectory = requireNonNull(workingDirectory);
            return this;
        }

        public Builder environmentVariables(final List<String> environmentVariables) {
            this.environmentVariables = requireNonNull(environmentVariables);
            return this;
        }

        public Builder ignoreErrors(final boolean ignoreErrors) {
            this.ignoreErrors = ignoreErrors;
            return this;
        }

        public Builder expectedExitValue(final int expectedExitValue) {
            this.expectedExitValue = expectedExitValue;
            return this;
        }

        public Builder commandTimeout(final Duration commandTimeout) {
            this.commandTimeout = requireNonNull(commandTimeout);
            return this;
        }

        public CommandExecutionStrategy build() {
            return new CommandExecutionStrategy(this);
        }

        public CommandResult execute(final CommandRunnerContext context) {
            return build().execute(context);
        }
    }
}
