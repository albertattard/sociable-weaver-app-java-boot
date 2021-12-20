package aa.sw.book;

import aa.sw.common.Result;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

import static aa.sw.MockHttpUtils.get;
import static aa.sw.MockHttpUtils.post;
import static aa.sw.MockHttpUtils.put;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;
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
            return mockMvc.perform(get("/api/book", params));
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
            return mockMvc.perform(get("/api/chapter", params));
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
            return mockMvc.perform(put("/api/entry", parameters, entry));
        }
    }

    @Nested
    class CreateEntryTest {

        @Test
        void saveAndReturnEntry() throws Exception {
            /* Given */
            final Path bookPath = Path.of("path-to-book");
            final Path chapterPath = Path.of("path-to-chapter-1");
            final Map<String, Object> params = Map.of("bookPath", bookPath, "chapterPath", chapterPath);
            final CreateEntry entry = createEntry();
            when(service.createEntry(ChapterPath.of(bookPath, chapterPath), entry))
                    .thenReturn(Result.value(BookControllerTest.createEntry()));

            /* When */
            final ResultActions result = makeCreateEntryRequest(params, entry);

            /* Then */
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("type", is("markdown")));
        }

        @Test
        void returnClientErrorWhenEntryDoesNotExists() throws Exception {
            /* Given */
            final Path bookPath = Path.of("path-to-book");
            final Path chapterPath = Path.of("path-to-chapter-1");
            final Map<String, Object> params = Map.of("bookPath", bookPath, "chapterPath", chapterPath);
            final CreateEntry entry = createEntry();
            when(service.createEntry(ChapterPath.of(bookPath, chapterPath), entry))
                    .thenReturn(Result.error(new EntryNotFoundException()));

            /* When */
            final ResultActions result = makeCreateEntryRequest(params, entry);

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
            final CreateEntry entry = createEntry();
            when(service.createEntry(ChapterPath.of(bookPath, chapterPath), entry))
                    .thenReturn(Result.error(new Exception("Simulating an error")));

            /* When */
            final ResultActions result = makeCreateEntryRequest(params, entry);

            /* Then */
            result.andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("message", is("Encountered an unexpected error")));
        }

        private CreateEntry createEntry() {
            return CreateEntry.builder().type("markdown").afterEntryWithId(UUID.randomUUID()).build();
        }

        private ResultActions makeCreateEntryRequest(final Map<String, Object> parameters, CreateEntry entry) throws Exception {
            return mockMvc.perform(post("/api/entry", parameters, entry));
        }
    }

    private static Chapter.Entry createEntry() {
        return Chapter.Entry.builder().id(UUID.randomUUID()).type("markdown").build();
    }
}
