package aa.sw.command;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Value
@Builder
@JsonDeserialize(builder = Command.CommandBuilder.class)
public class Command {
    String type;
    UUID id;
    String name;
    String workingDirectory;
    List<String> parameters;
    List<String> variables;
    Map<String, String> values;
    Boolean ignoreErrors;
    Boolean pushChanges;
    Boolean dryRun;
    Boolean visible;
    Boolean sensitive;
    Integer expectedExitValue;
    Duration commandTimeout;

    @JsonPOJOBuilder(withPrefix = "")
    public static class CommandBuilder { }
}
