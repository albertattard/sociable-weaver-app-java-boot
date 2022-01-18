package aa.sw.book;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

@Value
@Builder(toBuilder = true)
@JsonDeserialize(builder = Entry.EntryBuilder.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Entry {

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
    Boolean sensitive;
    Integer expectedExitValue;
    Duration commandTimeout;

    public static Entry chapter(final String title, final String description) {
        return Entry.builder()
                .id(UUID.randomUUID())
                .type("chapter")
                .parameters(List.of("Title:1", title, "Description:1", description))
                .build();
    }

    public <T> T map(final Function<Entry, T> mapper) {
        requireNonNull(mapper);

        return mapper.apply(this);
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class EntryBuilder {}
}
