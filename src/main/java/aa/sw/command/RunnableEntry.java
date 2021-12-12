package aa.sw.command;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

@Value
@Builder
@JsonDeserialize(builder = RunnableEntry.RunnableEntryBuilder.class)
public class RunnableEntry {

    String type;
    UUID id;
    String name;
    String workPath;
    String workingDirectory;
    List<String> parameters;
    List<String> variables;
    List<String> environmentVariables;
    Map<String, String> values;
    Boolean ignoreErrors;
    Boolean pushChanges;
    Boolean dryRun;
    Integer expectedExitValue;
    Duration commandTimeout;

    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }

    public String getWorkspace() {
        return workPath;
    }

    public Optional<String> getWorkingDirectory() {
        return Optional.ofNullable(workingDirectory);
    }

    public Optional<List<String>> getParameters() {
        return Optional.ofNullable(parameters);
    }

    public Optional<String> getParameterAt(final int index) {
        return getParameters()
                .filter(hasEnoughElements(index))
                .map(list -> list.get(index));
    }

    public String getParameterAtOrFail(final int index, final String message) {
        return getParameterAt(index)
                .orElseThrow(() -> new IllegalArgumentException(message));
    }

    public String joinParametersWithOrFail(final String delimiter) {
        requireNonNull(delimiter);

        return getParameters()
                .map(p -> String.join(delimiter, p))
                .orElseThrow(() -> new IllegalArgumentException("Missing parameters"));
    }

    public Stream<String> getParametersAsStream() {
        return getParameters().stream().flatMap(Collection::stream);
    }

    private Predicate<List<String>> hasEnoughElements(final int index) {
        return list -> index < list.size();
    }

    public Optional<List<String>> getVariables() {
        return Optional.ofNullable(variables);
    }

    public Optional<List<String>> getEnvironmentVariables() {
        return Optional.ofNullable(environmentVariables);
    }

    public Optional<Map<String, String>> getValues() {
        return Optional.ofNullable(values);
    }

    public Optional<Boolean> getIgnoreErrors() {
        return Optional.ofNullable(ignoreErrors);
    }

    public boolean isIgnoreErrors() {
        return Boolean.TRUE.equals(ignoreErrors);
    }

    public Optional<Boolean> getPushChanges() {
        return Optional.ofNullable(pushChanges);
    }

    public boolean isPushChanges() {
        return Boolean.TRUE.equals(pushChanges);
    }

    public Optional<Boolean> getDryRun() {
        return Optional.ofNullable(dryRun);
    }

    public boolean isDryRun() {
        return Boolean.TRUE.equals(dryRun);
    }

    public OptionalInt getExpectedExitValue() {
        return expectedExitValue == null ? OptionalInt.empty() : OptionalInt.of(expectedExitValue);
    }

    public Optional<Duration> getCommandTimeout() {
        return Optional.ofNullable(commandTimeout);
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class RunnableEntryBuilder { }
}
