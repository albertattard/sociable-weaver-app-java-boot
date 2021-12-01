package aa.sw.open;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OpenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OpenService openService;

    @Test
    void returnBookWhenFolderExistsAndValid() throws Exception {
        /* Given */
        final Path path = Path.of("somewhere");
        final Map<String, String> params = Map.of("path", path.toString());
        final OpenValue book = OpenValue.builder()
                .title("Test Book")
                .description("Test Description")
                .path(path.toString())
                .chapter("Chapter 1", "Test chapter 1", "chapter-1")
                .chapter("Chapter 2", "Test chapter 2", "chapter-2")
                .build();
        when(openService.openLocal(path)).thenReturn(Result.value(book));

        /* When */
        final ResultActions result = makeOpenLocalRequest(params);

        /* Then */
        result.andExpect(status().isOk())
                .andExpect(jsonPath("title", is("Test Book")))
                .andExpect(jsonPath("description", is("Test Description")))
                .andExpect(jsonPath("path", is(path.toString())))
                .andExpect(jsonPath("chapters", hasSize(2)))
                .andExpect(jsonPath("chapters[0].title", is("Chapter 1")))
                .andExpect(jsonPath("chapters[0].description", is("Test chapter 1")))
                .andExpect(jsonPath("chapters[0].path", is("chapter-1")))
                .andExpect(jsonPath("chapters[1].title", is("Chapter 2")))
                .andExpect(jsonPath("chapters[1].description", is("Test chapter 2")))
                .andExpect(jsonPath("chapters[1].path", is("chapter-2")));
    }

    @Test
    void returnClientErrorWhenFolderDoesNotExists() throws Exception {
        /* Given */
        final Path path = Path.of("somewhere");
        final Map<String, String> params = Map.of("path", path.toString());
        when(openService.openLocal(path)).thenReturn(Result.error(new FileNotFoundException()));

        /* When */
        final ResultActions result = makeOpenLocalRequest(params);

        /* Then */
        result.andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("message", is("Folder not found")));
    }

    @Test
    void returnClientErrorWhenAnUnexpectedErrorOccurs() throws Exception {
        /* Given */
        final Path path = Path.of("somewhere");
        final Map<String, String> params = Map.of("path", path.toString());
        when(openService.openLocal(path)).thenReturn(Result.error(new Exception()));

        /* When */
        final ResultActions result = makeOpenLocalRequest(params);

        /* Then */
        result.andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("message", is("Encountered an unexpected error")));
    }

    private ResultActions makeOpenLocalRequest(final Map<String, String> params) throws Exception {
        return mockMvc.perform(createGetRequest("/api/open-local", params));
    }

    /* TODO: Move this method into a more generic place */
    private MockHttpServletRequestBuilder createGetRequest(final String path, final Map<String, String> params) {
        final MockHttpServletRequestBuilder builder = get(path);
        params.forEach(builder::param);
        return builder;
    }

    /* TODO: Move this method into a more generic place */
    private ResultActions postJson(final String path, final Map<String, Object> body) throws Exception {
        return mockMvc.perform(createPostJsonRequest(path, body));
    }

    /* TODO: Move this method into a more generic place */
    private MockHttpServletRequestBuilder createPostJsonRequest(final String path, final Map<String, Object> body) {
        final MediaType APPLICATION_JSON_UTF8 = new MediaType(MediaType.APPLICATION_JSON.getType(),
                MediaType.APPLICATION_JSON.getSubtype(), StandardCharsets.UTF_8);

        return post(path)
                .contentType(APPLICATION_JSON_UTF8)
                .content(toJson(body));
    }

    /* TODO: Move this method into a more generic place */
    private static String toJson(final Map<String, Object> object) {
        final ObjectMapper mapper = JsonMapper.builder()
                .configure(SerializationFeature.WRAP_ROOT_VALUE, false)
                .build();
        final ObjectWriter writer = mapper.writer().withDefaultPrettyPrinter();
        return uncheckedIo(() -> writer.writeValueAsString(object));
    }

    /* TODO: Move this method into a more generic place */
    private static <T> T uncheckedIo(final IoSupplier<T> supplier) {
        try {
            return supplier.get();
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /* TODO: Move this method into a more generic place */
    private interface IoSupplier<T> {
        T get() throws IOException;
    }
}
