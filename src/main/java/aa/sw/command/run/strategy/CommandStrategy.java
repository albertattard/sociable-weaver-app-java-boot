package aa.sw.command.run.strategy;

import aa.sw.command.CommandResult;
import aa.sw.command.RunnableEntry;
import aa.sw.command.run.Command;
import aa.sw.command.run.CommandFormatter;
import aa.sw.command.run.CommandRunnerContext;
import aa.sw.command.run.ProcessResult;
import aa.sw.command.run.ProcessRunner;
import aa.sw.command.run.RunnableEntryExecutionStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public class CommandStrategy implements RunnableEntryExecutionStrategy {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandStrategy.class);

    private final Command command;
    private final List<String> environmentVariables;
    private final boolean ignoreErrors;
    private final int expectedExitValue;
    private final Duration commandTimeout;

    public static RunnableEntryExecutionStrategy of(final RunnableEntry entry) {
        requireNonNull(entry);

        final List<String> parameters = entry.getParameters()
                .orElseThrow(() -> new IllegalArgumentException("Missing command"));

        final Command command = Command.parse(parameters)
                .withInterpolatedValues(entry.getValues())
                .withWorkspace(entry.getWorkPath())
                .withWorkingDirectory(entry.getWorkingDirectory());

        return new Builder(command)
                .environmentVariables(entry.getEnvironmentVariables().orElse(Collections.emptyList()))
                .ignoreErrors(entry.getIgnoreErrors().orElse(false))
                .expectedExitValue(entry.getExpectedExitValue().orElse(0))
                .commandTimeout(entry.getCommandTimeout().orElse(Duration.ofMinutes(5)))
                .build();
    }

    private CommandStrategy(final Builder builder) {
        requireNonNull(builder);

        this.command = builder.command;
        this.environmentVariables = builder.environmentVariables;
        this.ignoreErrors = builder.ignoreErrors;
        this.expectedExitValue = builder.expectedExitValue;
        this.commandTimeout = builder.commandTimeout;
    }

    @Override
    public CommandResult execute(final CommandRunnerContext context) {
        context.appendLine(CommandFormatter.format(command));

        return ProcessRunner.builder()
                .context(context)
                .command(command.getCommandAndArgs())
                .workspace(command.getWorkspace().toFile())
                .workingDirectory(command.getWorkingDirectory())
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
                CommandFormatter.format(command), exitValue, expectedExitValue);
    }

    public static Builder builder(final Command command) {
        requireNonNull(command);

        return new Builder(command);
    }

    public static class Builder {
        private final Command command;
        private List<String> environmentVariables = Collections.emptyList();
        private boolean ignoreErrors;
        private int expectedExitValue = 0;
        private Duration commandTimeout = Duration.ofMinutes(5);

        private Builder(final Command command) {
            this.command = requireNonNull(command);
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

        public CommandStrategy build() {
            return new CommandStrategy(this);
        }

        public CommandResult execute(final CommandRunnerContext context) {
            return build().execute(context);
        }
    }
}
