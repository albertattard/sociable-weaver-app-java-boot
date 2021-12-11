package aa.sw.command.exec;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.OptionalInt;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ProcessResult {

    ExitState exitState;
    OptionalInt exitValue;

    enum ExitState {
        NOT_STARTED,
        TIMED_OUT,
        FINISHED_IN_TIME
    }

    private static final ProcessResult NOT_STARTED = new ProcessResult(ExitState.NOT_STARTED, OptionalInt.empty());

    public static ProcessResult notStarted() {
        return NOT_STARTED;
    }

    public static ProcessResult timedOut() {
        return new ProcessResult(ExitState.FINISHED_IN_TIME, OptionalInt.empty());
    }

    public static ProcessResult finishedWithExitValue(final int exitValue) {
        return new ProcessResult(ExitState.FINISHED_IN_TIME, OptionalInt.of(exitValue));
    }

    public <T> T map(final Function<ProcessResult, T> mapper) {
        requireNonNull(mapper);
        return mapper.apply(this);
    }
}
