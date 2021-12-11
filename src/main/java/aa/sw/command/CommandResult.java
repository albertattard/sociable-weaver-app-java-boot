package aa.sw.command;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CommandResult {

    Outcome outcome;

    private enum Outcome {
        COMMAND_NOT_FOUND(true),
        EXECUTION_STRATEGY_NOT_FOUND(true),
        DRY_RUN(false),
        FINISHED_AS_EXPECTED(false),
        FINISHED_WITH_SUPPRESSED_ERROR(false),
        FINISHED_NOT_AS_EXPECTED(true),
        FAILED_TO_START(true),
        TIMED_OUT(true);

        private final boolean isFailure;

        Outcome(boolean isFailure) { this.isFailure = isFailure; }

        public boolean isFailure() {
            return isFailure;
        }

        public <T> T map(final Function<Outcome, T> mapper) {
            requireNonNull(mapper);

            return mapper.apply(this);
        }
    }

    public static CommandResult commandNotFound() { return of(Outcome.COMMAND_NOT_FOUND); }

    public static CommandResult executionStrategyNotFound() { return of(Outcome.EXECUTION_STRATEGY_NOT_FOUND); }

    public static CommandResult dryRun() { return of(Outcome.DRY_RUN); }

    public static CommandResult finishedAsExpected() { return of(Outcome.FINISHED_AS_EXPECTED); }

    public static CommandResult finishedWithSuppressedError() { return of(Outcome.FINISHED_WITH_SUPPRESSED_ERROR); }

    public static CommandResult finishedNotAsExpected() { return of(Outcome.FINISHED_NOT_AS_EXPECTED); }

    public static CommandResult failedToStart() { return of(Outcome.FAILED_TO_START); }

    public static CommandResult timedOut() { return of(Outcome.TIMED_OUT); }

    private static CommandResult of(final Outcome outcome) {
        requireNonNull(outcome);

        return new CommandResult(outcome);
    }

    public <T> CommandResult thenIfPresent(final Optional<T> optional, final Function<T, CommandResult> next) {
        requireNonNull(optional);
        requireNonNull(next);

        return optional
                .map(value -> then(() -> next.apply(value)))
                .orElse(this);
    }

    public CommandResult thenIfTrue(final boolean assertion, final Supplier<CommandResult> next) {
        requireNonNull(next);

        if (assertion) {
            return then(next);
        }

        return this;
    }

    public CommandResult then(final Supplier<CommandResult> next) {
        requireNonNull(next);

        if (hasFailed()) {
            return this;
        }

        return next.get();
    }

    public boolean hasFailed() {
        return outcome.isFailure();
    }

    public String asString() {
        return outcome.name();
    }

    @Override
    public String toString() {
        return asString();
    }
}
