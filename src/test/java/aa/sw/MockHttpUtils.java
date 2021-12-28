package aa.sw;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static aa.sw.common.UncheckedIo.uncheckedIo;
import static java.util.Objects.requireNonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MockHttpUtils {

    private static final MediaType APPLICATION_JSON_UTF8 = createAppJsonUtf8();

    public static MockHttpServletRequestBuilder get(final String path, final Map<String, Object> parameters) {
        requireNonNull(path);
        requireNonNull(parameters);

        return setParameters(MockMvcRequestBuilders.get(path), parameters);
    }

    public static MockHttpServletRequestBuilder post(final String path,
                                                     final Map<String, Object> parameters,
                                                     final Object body) {
        requireNonNull(path);
        requireNonNull(parameters);
        requireNonNull(body);

        return populateRequest(MockMvcRequestBuilders.post(path), parameters, body);
    }

    public static MockHttpServletRequestBuilder put(final String path,
                                                    final Map<String, Object> parameters,
                                                    final Object body) {
        requireNonNull(path);
        requireNonNull(parameters);
        requireNonNull(body);

        return populateRequest(MockMvcRequestBuilders.put(path), parameters, body);
    }

    private static MockHttpServletRequestBuilder populateRequest(final MockHttpServletRequestBuilder builder,
                                                                 final Map<String, Object> parameters,
                                                                 final Object body) {
        return setParameters(builder, parameters)
                .contentType(APPLICATION_JSON_UTF8)
                .content(toJson(body));
    }

    private static MockHttpServletRequestBuilder setParameters(final MockHttpServletRequestBuilder builder,
                                                               final Map<String, Object> parameters) {
        parameters.forEach((k, v) -> builder.param(k, v.toString()));
        return builder;
    }

    public static String toJson(final Object object) {
        requireNonNull(object);

        final ObjectMapper mapper = JsonMapper.builder()
                .addModule(new Jdk8Module())
                .addModule(new JavaTimeModule())
                .configure(SerializationFeature.WRAP_ROOT_VALUE, false)
                .build();
        final ObjectWriter writer = mapper.writer().withDefaultPrettyPrinter();
        return uncheckedIo(() -> writer.writeValueAsString(object));
    }

    private static MediaType createAppJsonUtf8() {
        return new MediaType(
                MediaType.APPLICATION_JSON.getType(),
                MediaType.APPLICATION_JSON.getSubtype(),
                StandardCharsets.UTF_8);
    }
}
