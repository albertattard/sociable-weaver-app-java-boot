package aa.sw.command.run.strategy;

import aa.sw.command.CommandResult;
import aa.sw.command.RunnableEntry;
import aa.sw.command.run.Command;
import aa.sw.command.run.CommandRunnerContext;
import aa.sw.command.run.ProcessResult;
import aa.sw.command.run.ProcessRunner;
import aa.sw.command.run.RunnableEntryExecutionStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class CommandStrategy implements RunnableEntryExecutionStrategy {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandStrategy.class);

    private final Command command;
    private final boolean ignoreErrors;
    private final int expectedExitValue;
    private final Duration commandTimeout;

    public static RunnableEntryExecutionStrategy of(final RunnableEntry entry) {
        requireNonNull(entry);

        final List<String> parameters = entry.getParameters()
                .orElseThrow(() -> new IllegalArgumentException("Missing command"));

        final Command command = Command.parse(parameters)
                .withWorkspace(entry.getWorkspace())
                .withWorkingDirectory(entry.getWorkingDirectory())
                .withEnvironmentVariables(entry.getEnvironmentVariables().orElse(Collections.emptyList()))
                .withInterpolatedValues(entry.getValues());

        return new Builder(command)
                .ignoreErrors(entry.getIgnoreErrors().orElse(false))
                .expectedExitValue(entry.getExpectedExitValue().orElse(0))
                .commandTimeout(entry.getCommandTimeout().orElse(Duration.ofSeconds(5)))
                .build();
    }

    private CommandStrategy(final Builder builder) {
        requireNonNull(builder);

        this.command = builder.command;
        this.ignoreErrors = builder.ignoreErrors;
        this.expectedExitValue = builder.expectedExitValue;
        this.commandTimeout = builder.commandTimeout;
    }

    @Override
    public CommandResult execute(final CommandRunnerContext context) {
        context.appendLine(command.asFormattedString());

        return ProcessRunner.builder()
                .context(context)
                .command(command.getCommandAndArgs())
                .executionDirectory(command.getExecutionDirectory())
                .environmentVariables(command.getEnvironmentVariables())
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
        LOGGER.debug("Command {} finished with exit value {} (expecting {})", command, exitValue, expectedExitValue);
    }

    public static Builder builder(final Command command) {
        requireNonNull(command);

        return new Builder(command);
    }

    public static class Builder {
        private final Command command;
        private boolean ignoreErrors;
        private int expectedExitValue = 0;
        private Duration commandTimeout = Duration.ofMinutes(5);

        private Builder(final Command command) {
            this.command = requireNonNull(command);
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

        public CommandStrategy build() {
            return new CommandStrategy(this);
        }

        public CommandResult execute(final CommandRunnerContext context) {
            return build().execute(context);
        }
    }
}
