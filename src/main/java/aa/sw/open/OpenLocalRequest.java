package aa.sw.open;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonDeserialize(builder = OpenLocalRequest.OpenLocalRequestBuilder.class)
public class OpenLocalRequest {

    String openFromFolder;

    @JsonPOJOBuilder(withPrefix = "")
    public static class OpenLocalRequestBuilder { }
}
