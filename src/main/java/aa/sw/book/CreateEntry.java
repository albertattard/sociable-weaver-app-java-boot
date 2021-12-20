package aa.sw.book;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
@JsonDeserialize(builder = CreateEntry.CreateEntryBuilder.class)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CreateEntry {

    String type;
    UUID afterEntryWithId;

    @JsonPOJOBuilder(withPrefix = "")
    public static class CreateEntryBuilder {}
}
