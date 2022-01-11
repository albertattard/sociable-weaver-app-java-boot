package aa.sw.book;

import aa.sw.common.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static aa.sw.IoUtils.copyDirectory;
import static aa.sw.IoUtils.emptyDirectory;
import static aa.sw.book.Fixtures.prologueFile;
import static org.assertj.core.api.Assertions.assertThat;

class BookServiceTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private final BookService service = new BookService(mapper);

    private static final Path BOOK_DIRECTORY = Path.of("build/fixtures/books");
    private static final ChapterPath CHAPTER_PATH = ChapterPath.of(BOOK_DIRECTORY, Fixtures.PROLOGUE_FILE);

    @Nested
    class OpenBookTest {

        @Test
        void openBook() {
            /* Given */
            final BookPath bookPath = BookPath.of(Fixtures.BOOK_DIRECTORY);

            /* When */
            final Result<Book> book = service.openBook(bookPath);

            /* Then */
            assertThat(book)
                    .isEqualTo(Result.value(Fixtures.BOOK));
        }
    }

    @Nested
    class ReadChapterTest {

        @Test
        void readChapter() {
            final Result<Chapter> chapter = service.readChapter(Fixtures.PROLOGUE_CHAPTER_PATH);

            assertThat(chapter)
                    .isEqualTo(Result.value(Fixtures.PROLOGUE));
        }
    }

    @Nested
    class CreateEntryTest {

        private static final Path BOOK_DIRECTORY = Path.of("build/fixtures/books");

        private static final ChapterPath CHAPTER_PATH = ChapterPath.of(BOOK_DIRECTORY, Fixtures.PROLOGUE_FILE);

        @BeforeEach
        void setUp() {
            emptyDirectory(BOOK_DIRECTORY);
            copyDirectory(Fixtures.BOOK_DIRECTORY, BOOK_DIRECTORY);
        }

        @Test
        void returnsErrorWhenEntryNotFoundInChapter() {
            /* Given */
            final CreateEntry entry = CreateEntry.builder()
                    .type("markdown")
                    .afterEntryWithId(UUID.randomUUID())
                    .build();

            /* When */
            final Result<Chapter.Entry> result = service.createEntry(CHAPTER_PATH, entry);

            /* Then */
            assertThat(result)
                    .isEqualTo(Result.error(new EntryNotFoundException()));
        }


        @Test
        void createAndReturnTheWrittenEntry() throws Exception {
            /* Given */
            final CreateEntry entry = CreateEntry.builder()
                    .type("markdown")
                    .afterEntryWithId(Fixtures.PROLOGUE_ENTRY_2.getId())
                    .build();

            /* When */
            final Result<Chapter.Entry> result = service.createEntry(CHAPTER_PATH, entry);

            /* Then */
            assertThat(result.isValuePresent()).isTrue();
            assertThat(result.value().getType()).isEqualTo("markdown");

            final Chapter chapter = mapper.readValue(prologueFile(BOOK_DIRECTORY), Chapter.class);
            assertThat(chapter)
                    .isEqualTo(Chapter.builder()
                            .entry(Fixtures.PROLOGUE_ENTRY_1)
                            .entry(Fixtures.PROLOGUE_ENTRY_2)
                            .entry(result.value())
                            .build());
        }
    }

    @Nested
    class SaveEntryTest {

        private static final Path BOOK_DIRECTORY = Path.of("build/fixtures/books");

        private ChapterPath chapterPath;

        @BeforeEach
        void setUp() {
            emptyDirectory(BOOK_DIRECTORY);
            copyDirectory(Fixtures.BOOK_DIRECTORY, BOOK_DIRECTORY);

            /* Create the path after copying the fixtures as otherwise the path will be wrongly constructed.  If the
                book path is not a directory, then it will be assumed as the book path.
                TODO: we may need to reconsider this approach as it can be misleading.  */
            chapterPath = ChapterPath.of(BOOK_DIRECTORY, Fixtures.PROLOGUE_FILE);
        }

        @Test
        void returnsErrorWhenEntryNotFoundInChapter() {
            /* Given */
            final Chapter.Entry entry = Fixtures.PROLOGUE_ENTRY_2.toBuilder()
                    .id(UUID.randomUUID())
                    .build();

            /* When */
            final Result<Chapter.Entry> result = service.saveEntry(chapterPath, entry);

            /* Then */
            assertThat(result)
                    .isEqualTo(Result.error(new EntryNotFoundException()));
        }

        @Test
        void saveAndReturnTheWrittenEntry() throws Exception {
            /* Given */
            final Chapter.Entry updatedEntry = Fixtures.PROLOGUE_ENTRY_2.toBuilder()
                    .parameters(List.of("I make mistakes, and I make more mistakes, and some more, and that's how I learn."))
                    .build();

            /* When */
            final Result<Chapter.Entry> result = service.saveEntry(chapterPath, updatedEntry);

            /* Then */
            assertThat(result)
                    .isEqualTo(Result.value(updatedEntry));

            final Chapter chapter = mapper.readValue(prologueFile(BOOK_DIRECTORY), Chapter.class);
            assertThat(chapter)
                    .isEqualTo(Chapter.builder()
                            .entry(Fixtures.PROLOGUE_ENTRY_1)
                            .entry(updatedEntry)
                            .build());
        }
    }

    @Nested
    class DeleteEntryTest {

        private static final Path BOOK_DIRECTORY = Path.of("build/fixtures/books");

        private static final ChapterPath CHAPTER_PATH = ChapterPath.of(BOOK_DIRECTORY, Fixtures.PROLOGUE_FILE);

        @BeforeEach
        void setUp() {
            emptyDirectory(BOOK_DIRECTORY);
            copyDirectory(Fixtures.BOOK_DIRECTORY, BOOK_DIRECTORY);
        }

        @Test
        void returnsErrorWhenEntryNotFoundInChapter() {
            /* Given */
            final UUID entryId = UUID.randomUUID();

            /* When */
            final Result<Chapter.Entry> result = service.deleteEntry(CHAPTER_PATH, entryId);

            /* Then */
            assertThat(result)
                    .isEqualTo(Result.error(new EntryNotFoundException()));
        }

        @Test
        void deleteFirstEntryAndReturnTheEntry() throws Exception {
            /* Given */
            final UUID entryId = Fixtures.PROLOGUE_ENTRY_1.getId();

            /* When */
            final Result<Chapter.Entry> result = service.deleteEntry(CHAPTER_PATH, entryId);

            /* Then */
            assertThat(result.isValuePresent()).isTrue();
            assertThat(result.value().getId()).isEqualTo(entryId);

            final Chapter chapter = mapper.readValue(prologueFile(BOOK_DIRECTORY), Chapter.class);
            assertThat(chapter)
                    .isEqualTo(Chapter.builder()
                            .entry(Fixtures.PROLOGUE_ENTRY_2)
                            .build());
        }

        @Test
        void deleteLastEntryAndReturnTheEntry() throws Exception {
            /* Given */
            final UUID entryId = Fixtures.PROLOGUE_ENTRY_2.getId();

            /* When */
            final Result<Chapter.Entry> result = service.deleteEntry(CHAPTER_PATH, entryId);

            /* Then */
            assertThat(result.isValuePresent()).isTrue();
            assertThat(result.value().getId()).isEqualTo(entryId);

            final Chapter chapter = mapper.readValue(prologueFile(BOOK_DIRECTORY), Chapter.class);
            assertThat(chapter)
                    .isEqualTo(Chapter.builder()
                            .entry(Fixtures.PROLOGUE_ENTRY_1)
                            .build());
        }
    }
}
