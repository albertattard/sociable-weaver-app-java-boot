package aa.sw.book;

import aa.sw.common.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

class BookServiceTest {

    private final BookData data = mock(BookData.class);
    private final BookService service = new BookService(data);

    @BeforeEach
    void setUp() {
        reset(data);
    }

    @Nested
    class OpenBookTest {

        @Test
        void openBook() {
            /* Given */
            final BookPath bookPath = BookPath.of(Fixtures.BOOK_DIRECTORY);
            when(data.readBook(eq(bookPath.getPath()))).thenReturn(Result.value(Fixtures.BOOK));

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
            /* Given */
            final ChapterPath chapterPath = Fixtures.PROLOGUE_CHAPTER_PATH;
            when(data.readChapter(eq(chapterPath.getPath()))).thenReturn(Result.value(Fixtures.PROLOGUE));

            /* When */
            final Result<Chapter> chapter = service.readChapter(chapterPath);

            /* Then */
            assertThat(chapter)
                    .isEqualTo(Result.value(Fixtures.PROLOGUE));
        }
    }

    @Nested
    class CreateEntryTest {

        @Test
        void returnsErrorWhenEntryNotFoundInChapter() {
            /* Given */
            final CreateEntry entry = CreateEntry.builder()
                    .type("markdown")
                    .afterEntryWithId(UUID.randomUUID())
                    .build();
            final ChapterPath chapterPath = Fixtures.PROLOGUE_CHAPTER_PATH;
            when(data.readChapter(eq(chapterPath.getPath()))).thenReturn(Result.value(Fixtures.PROLOGUE));

            /* When */
            final Result<Entry> result = service.createEntry(chapterPath, entry);

            /* Then */
            assertThat(result)
                    .isEqualTo(Result.error(new EntryNotFoundException()));
        }


        @Test
        void createAndReturnTheWrittenEntry() {
            /* Given */
            final CreateEntry entry = CreateEntry.builder()
                    .type("markdown")
                    .afterEntryWithId(Fixtures.PROLOGUE_ENTRY_2.getId())
                    .build();
            final ChapterPath chapterPath = Fixtures.PROLOGUE_CHAPTER_PATH;
            when(data.readChapter(eq(chapterPath.getPath()))).thenReturn(Result.value(Fixtures.PROLOGUE));
            when(data.writeChapter(eq(chapterPath.getPath()), any(Chapter.class))).thenAnswer(
                    (Answer<Result<Chapter>>) invocation -> Result.value((Chapter) invocation.getArguments()[1])
            );

            /* When */
            final Result<Entry> result = service.createEntry(chapterPath, entry);

            /* Then */
            assertThat(result.isValuePresent()).isTrue();
            assertThat(result.value().getType()).isEqualTo("markdown");
        }
    }

    @Nested
    class SaveEntryTest {

        @Test
        void returnsErrorWhenEntryNotFoundInChapter() {
            /* Given */
            final Entry entry = Fixtures.PROLOGUE_ENTRY_2.toBuilder()
                    .id(UUID.randomUUID())
                    .build();
            final ChapterPath chapterPath = Fixtures.PROLOGUE_CHAPTER_PATH;
            when(data.readChapter(eq(chapterPath.getPath()))).thenReturn(Result.value(Fixtures.PROLOGUE));

            /* When */
            final Result<Entry> result = service.saveEntry(chapterPath, entry);

            /* Then */
            assertThat(result)
                    .isEqualTo(Result.error(new EntryNotFoundException()));
        }

        @Test
        void saveAndReturnTheWrittenEntry() throws Exception {
            /* Given */
            final Entry updatedEntry = Fixtures.PROLOGUE_ENTRY_2.toBuilder()
                    .parameters(List.of("I make mistakes, and I make more mistakes, and some more, and that's how I learn."))
                    .build();
            final ChapterPath chapterPath = Fixtures.PROLOGUE_CHAPTER_PATH;
            when(data.readChapter(eq(chapterPath.getPath()))).thenReturn(Result.value(Fixtures.PROLOGUE));
            when(data.writeChapter(eq(chapterPath.getPath()), any(Chapter.class))).thenAnswer(
                    (Answer<Result<Chapter>>) invocation -> Result.value((Chapter) invocation.getArguments()[1])
            );

            /* When */
            final Result<Entry> result = service.saveEntry(chapterPath, updatedEntry);

            /* Then */
            assertThat(result)
                    .isEqualTo(Result.value(updatedEntry));
        }
    }

    @Nested
    class DeleteEntryTest {

        @Test
        void returnsErrorWhenEntryNotFoundInChapter() {
            /* Given */
            final UUID entryId = UUID.randomUUID();
            final ChapterPath chapterPath = Fixtures.PROLOGUE_CHAPTER_PATH;
            when(data.readChapter(eq(chapterPath.getPath()))).thenReturn(Result.value(Fixtures.PROLOGUE));

            /* When */
            final Result<Entry> result = service.deleteEntry(chapterPath, entryId);

            /* Then */
            assertThat(result)
                    .isEqualTo(Result.error(new EntryNotFoundException()));
        }

        @Test
        void deleteFirstEntryAndReturnTheEntry() {
            /* Given */
            final UUID entryId = Fixtures.PROLOGUE_ENTRY_1.getId();
            final ChapterPath chapterPath = Fixtures.PROLOGUE_CHAPTER_PATH;
            final Chapter writtenChapter = Chapter.builder().chapterPath("00-prologue.json").entry(Fixtures.PROLOGUE_ENTRY_2).build();
            when(data.readChapter(eq(chapterPath.getPath()))).thenReturn(Result.value(Fixtures.PROLOGUE));
            when(data.writeChapter(eq(chapterPath.getPath()), eq(writtenChapter))).thenReturn(Result.value(writtenChapter));

            /* When */
            final Result<Entry> result = service.deleteEntry(chapterPath, entryId);

            /* Then */
            assertThat(result.isValuePresent()).isTrue();
            assertThat(result.value().getId()).isEqualTo(entryId);
        }

        @Test
        void deleteLastEntryAndReturnTheEntry() {
            /* Given */
            final UUID entryId = Fixtures.PROLOGUE_ENTRY_2.getId();
            final ChapterPath chapterPath = Fixtures.PROLOGUE_CHAPTER_PATH;
            final Chapter writtenChapter = Chapter.builder().chapterPath("00-prologue.json").entry(Fixtures.PROLOGUE_ENTRY_1).build();
            when(data.readChapter(eq(chapterPath.getPath()))).thenReturn(Result.value(Fixtures.PROLOGUE));
            when(data.writeChapter(eq(chapterPath.getPath()), eq(writtenChapter))).thenReturn(Result.value(writtenChapter));

            /* When */
            final Result<Entry> result = service.deleteEntry(chapterPath, entryId);

            /* Then */
            assertThat(result.isValuePresent()).isTrue();
            assertThat(result.value().getId()).isEqualTo(entryId);
        }
    }
}
