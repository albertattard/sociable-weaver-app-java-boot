package aa.sw.book;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Nested;
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
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService service;

    @Nested
    class OpenBookTest {

        @Test
        void returnBookWhenFolderExistsAndValid() throws Exception {
            /* Given */
            final Path bookPath = Path.of("path-to-book");
            final Map<String, ?> params = Map.of("bookPath", bookPath);
            final Book book = Book.builder()
                    .title("Test Book")
                    .description("Test Description")
                    .chapter("Chapter 1", "Test chapter 1", "chapter-1")
                    .chapter("Chapter 2", "Test chapter 2", "chapter-2")
                    .build();
            when(service.openBook(bookPath)).thenReturn(Result.value(book));

            /* When */
            final ResultActions result = makeOpenBookRequest(params);

            /* Then */
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("title", is("Test Book")))
                    .andExpect(jsonPath("description", is("Test Description")))
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
            final Path bookPath = Path.of("path-to-book");
            final Map<String, ?> params = Map.of("bookPath", bookPath);
            when(service.openBook(bookPath)).thenReturn(Result.error(new FileNotFoundException()));

            /* When */
            final ResultActions result = makeOpenBookRequest(params);

            /* Then */
            result.andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("message", is("Book not found")));
        }

        @Test
        void returnClientErrorWhenAnUnexpectedErrorOccurs() throws Exception {
            /* Given */
            final Path bookPath = Path.of("path-to-book");
            final Map<String, ?> params = Map.of("bookPath", bookPath);
            when(service.openBook(bookPath)).thenReturn(Result.error(new Exception()));

            /* When */
            final ResultActions result = makeOpenBookRequest(params);

            /* Then */
            result.andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("message", is("Encountered an unexpected error")));
        }

        private ResultActions makeOpenBookRequest(final Map<String, ?> params) throws Exception {
            return mockMvc.perform(createGetRequest("/api/book/open", params));
        }
    }

    @Nested
    class ReadChapterTest {

        @Test
        void returnChapterWhenExistsAndValid() throws Exception {
            /* Given */
            final Path bookPath = Path.of("src/test/resources/fixtures/books");
            final Path chapterPath = Path.of("path-to-chapter-1");
            final Map<String, ?> params = Map.of("bookPath", bookPath, "chapterPath", chapterPath);
            final Chapter chapter = Chapter.builder()
                    .entry(Chapter.Entry.builder().build())
                    .entry(Chapter.Entry.builder().build())
                    .build();
            when(service.readChapter(bookPath, chapterPath)).thenReturn(Result.value(chapter));

            /* When */
            final ResultActions result = makeReadChapterRequest(params);

            /* Then */
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("entries", hasSize(2)));
        }

        @Test
        void returnClientErrorWhenChapterDoesNotExists() throws Exception {
            /* Given */
            final Path bookPath = Path.of("src/test/resources/fixtures/books");
            final Path chapterPath = Path.of("path-to-chapter-1");
            final Map<String, ?> params = Map.of("bookPath", bookPath, "chapterPath", chapterPath);
            when(service.readChapter(bookPath, chapterPath)).thenReturn(Result.error(new FileNotFoundException()));

            /* When */
            final ResultActions result = makeReadChapterRequest(params);

            /* Then */
            result.andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("message", is("Chapter not found")));
        }

        @Test
        void returnClientErrorWhenAnUnexpectedErrorOccurs() throws Exception {
            /* Given */
            final Path bookPath = Path.of("src/test/resources/fixtures/books");
            final Path chapterPath = Path.of("path-to-chapter-1");
            final Map<String, ?> params = Map.of("bookPath", bookPath, "chapterPath", chapterPath);
            when(service.readChapter(bookPath, chapterPath)).thenReturn(Result.error(new Exception()));

            /* When */
            final ResultActions result = makeReadChapterRequest(params);

            /* Then */
            result.andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("message", is("Encountered an unexpected error")));
        }

        private ResultActions makeReadChapterRequest(final Map<String, ?> params) throws Exception {
            return mockMvc.perform(createGetRequest("/api/book/read-chapter", params));
        }
    }

    /* TODO: Move this method into a more generic place */
    private MockHttpServletRequestBuilder createGetRequest(final String path, final Map<String, ?> params) {
        final MockHttpServletRequestBuilder builder = get(path);
        params.forEach((k, v) -> builder.param(k, v.toString()));
        return builder;
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
