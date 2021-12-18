package aa.sw.book;

import aa.sw.common.Result;
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

import static aa.sw.IoUtils.uncheckedIo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
            final BookPath bookPath = BookPath.of(Path.of("path-to-book"));
            final Map<String, Object> params = Map.of("bookPath", bookPath);
            final Book book = Book.builder()
                    .title("Test Book")
                    .description("Test Description")
                    .chapter("Chapter 1", "Test chapter 1", "chapter-1")
                    .chapter("Chapter 2", "Test chapter 2", "chapter-2")
                    .bookPath(bookPath.getPath())
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
                    .andExpect(jsonPath("chapters[1].path", is("chapter-2")))
                    .andExpect(jsonPath("bookPath", is("path-to-book")));
        }

        @Test
        void returnClientErrorWhenFolderDoesNotExists() throws Exception {
            /* Given */
            final BookPath bookPath = BookPath.of(Path.of("path-to-book"));
            final Map<String, Object> params = Map.of("bookPath", bookPath);
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
            final BookPath bookPath = BookPath.of(Path.of("path-to-book"));
            final Map<String, Object> params = Map.of("bookPath", bookPath);
            when(service.openBook(bookPath)).thenReturn(Result.error(new Exception("Simulating an error")));

            /* When */
            final ResultActions result = makeOpenBookRequest(params);

            /* Then */
            result.andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("message", is("Encountered an unexpected error")));
        }

        private ResultActions makeOpenBookRequest(final Map<String, Object> params) throws Exception {
            return mockMvc.perform(createGetRequest("/api/book", params));
        }
    }

    @Nested
    class ReadChapterTest {

        @Test
        void returnChapterWhenExistsAndValid() throws Exception {
            /* Given */
            final Path bookPath = Path.of("path-to-book");
            final Path chapterPath = Path.of("path-to-chapter-1");
            final Map<String, Object> params = Map.of("bookPath", bookPath, "chapterPath", chapterPath);
            final Chapter chapter = Fixtures.PROLOGUE;
            when(service.readChapter(ChapterPath.of(bookPath, chapterPath))).thenReturn(Result.value(chapter));

            /* When */
            final ResultActions result = makeReadChapterRequest(params);

            /* Then */
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("entries", hasSize(2)));
        }

        @Test
        void returnClientErrorWhenChapterDoesNotExists() throws Exception {
            /* Given */
            final Path bookPath = Path.of("path-to-book");
            final Path chapterPath = Path.of("path-to-chapter-1");
            final Map<String, Object> params = Map.of("bookPath", bookPath, "chapterPath", chapterPath);
            when(service.readChapter(ChapterPath.of(bookPath, chapterPath)))
                    .thenReturn(Result.error(new FileNotFoundException("Simulating an error")));

            /* When */
            final ResultActions result = makeReadChapterRequest(params);

            /* Then */
            result.andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("message", is("Chapter not found")));
        }

        @Test
        void returnClientErrorWhenAnUnexpectedErrorOccurs() throws Exception {
            /* Given */
            final Path bookPath = Path.of("path-to-book");
            final Path chapterPath = Path.of("path-to-chapter-1");
            final Map<String, Object> params = Map.of("bookPath", bookPath, "chapterPath", chapterPath);
            when(service.readChapter(ChapterPath.of(bookPath, chapterPath)))
                    .thenReturn(Result.error(new Exception("Simulating an error")));

            /* When */
            final ResultActions result = makeReadChapterRequest(params);

            /* Then */
            result.andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("message", is("Encountered an unexpected error")));
        }

        private ResultActions makeReadChapterRequest(final Map<String, Object> params) throws Exception {
            return mockMvc.perform(createGetRequest("/api/chapter", params));
        }
    }

    @Nested
    class SaveEntryTest {

        @Test
        void saveAndReturnEntry() throws Exception {
            /* Given */
            final Path bookPath = Path.of("path-to-book");
            final Path chapterPath = Path.of("path-to-chapter-1");
            final Map<String, Object> params = Map.of("bookPath", bookPath, "chapterPath", chapterPath);
            final Chapter.Entry entry = createEntry();
            when(service.saveEntry(ChapterPath.of(bookPath, chapterPath), entry))
                    .thenReturn(Result.value(entry));

            /* When */
            final ResultActions result = makeSaveEntryRequest(params, entry);

            /* Then */
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("id", is(entry.getId().toString())));
        }

        @Test
        void returnClientErrorWhenEntryDoesNotExists() throws Exception {
            /* Given */
            final Path bookPath = Path.of("path-to-book");
            final Path chapterPath = Path.of("path-to-chapter-1");
            final Map<String, Object> params = Map.of("bookPath", bookPath, "chapterPath", chapterPath);
            final Chapter.Entry entry = createEntry();
            when(service.saveEntry(ChapterPath.of(bookPath, chapterPath), entry))
                    .thenReturn(Result.error(new EntryNotFoundException()));

            /* When */
            final ResultActions result = makeSaveEntryRequest(params, entry);

            /* Then */
            result.andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("message", is("Entry not found in chapter")));
        }

        @Test
        void returnClientErrorWhenAnUnexpectedErrorOccurs() throws Exception {
            /* Given */
            final Path bookPath = Path.of("path-to-book");
            final Path chapterPath = Path.of("path-to-chapter-1");
            final Map<String, Object> params = Map.of("bookPath", bookPath, "chapterPath", chapterPath);
            final Chapter.Entry entry = createEntry();
            when(service.saveEntry(ChapterPath.of(bookPath, chapterPath), entry))
                    .thenReturn(Result.error(new Exception("Simulating an error")));

            /* When */
            final ResultActions result = makeSaveEntryRequest(params, entry);

            /* Then */
            result.andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("message", is("Encountered an unexpected error")));
        }

        private ResultActions makeSaveEntryRequest(final Map<String, Object> parameters, Chapter.Entry entry) throws Exception {
            return mockMvc.perform(createPutRequest("/api/entry", parameters, entry));
        }
    }

    private Chapter.Entry createEntry() {
        return Chapter.Entry.builder().id(UUID.randomUUID()).type("something").build();
    }

    /* TODO: Move this method into a more generic place */
    private MockHttpServletRequestBuilder createGetRequest(final String path, final Map<String, Object> parameters) {
        final MockHttpServletRequestBuilder builder = get(path);
        parameters.forEach((k, v) -> builder.param(k, v.toString()));
        return builder;
    }

    /* TODO: Move this method into a more generic place */
    private MockHttpServletRequestBuilder createPostRequest(final String path,
                                                            final Map<String, Object> parameters,
                                                            final Object body) {
        return populateRequest(post(path), parameters, body);
    }

    /* TODO: Move this method into a more generic place */
    private MockHttpServletRequestBuilder createPutRequest(final String path,
                                                           final Map<String, Object> parameters,
                                                           final Object body) {
        return populateRequest(put(path), parameters, body);
    }

    /* TODO: Move this method into a more generic place */
    private MockHttpServletRequestBuilder populateRequest(final MockHttpServletRequestBuilder builder,
                                                          final Map<String, Object> parameters,
                                                          final Object body) {
        final MediaType APPLICATION_JSON_UTF8 = new MediaType(
                MediaType.APPLICATION_JSON.getType(),
                MediaType.APPLICATION_JSON.getSubtype(),
                StandardCharsets.UTF_8);

        parameters.forEach((k, v) -> builder.param(k, v.toString()));

        return builder
                .contentType(APPLICATION_JSON_UTF8)
                .content(toJson(body));
    }

    /* TODO: Move this method into a more generic place */
    private static String toJson(final Object object) {
        final ObjectMapper mapper = JsonMapper.builder()
                .configure(SerializationFeature.WRAP_ROOT_VALUE, false)
                .build();
        final ObjectWriter writer = mapper.writer().withDefaultPrettyPrinter();
        return uncheckedIo(() -> writer.writeValueAsString(object));
    }
}
