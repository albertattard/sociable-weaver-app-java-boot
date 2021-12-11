package aa.sw.command;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;

@Value
@Builder
@JsonDeserialize(builder = RunnableEntry.RunnableEntryBuilder.class)
public class RunnableEntry {

    String type;
    UUID id;
    String name;
    String workingDirectory;
    List<String> parameters;
    List<String> variables;
    List<String> environmentVariables;
    Map<String, String> values;
    Boolean ignoreErrors;
    Boolean pushChanges;
    Boolean dryRun;
    Boolean visible;
    Boolean sensitive;
    Integer expectedExitValue;
    Duration commandTimeout;

    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }

    public Optional<String> getWorkingDirectory() {
        return Optional.ofNullable(workingDirectory);
    }

    public Optional<List<String>> getParameters() {
        return Optional.ofNullable(parameters);
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

    public Optional<Boolean> getVisible() {
        return Optional.ofNullable(visible);
    }

    public boolean isVisible() {
        return !Boolean.FALSE.equals(visible);
    }

    public Optional<Boolean> getSensitive() {
        return Optional.ofNullable(sensitive);
    }

    public boolean isSensitive() {
        return !Boolean.FALSE.equals(visible);
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
