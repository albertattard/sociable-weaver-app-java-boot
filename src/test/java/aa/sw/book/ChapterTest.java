package aa.sw.book;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ChapterTest {

    @Nested
    class TocTest {

        @Test
        void returnTheChapterTitleAndDescription() {
            /* Given */
            final Chapter chapter = Chapter.of("chapter-path", "Title", "Description");

            /* When */
            final String title = chapter.getTitle();
            final String description = chapter.getDescription();

            /* Then */
            assertThat(title).isEqualTo("Title");
            assertThat(description).isEqualTo("Description");
        }
    }

    @Nested
    class IndexOfTest {

        @Test
        void returnNegativeOneWhenEntryNotFound() {
            /* Given */
            final Chapter chapter = Fixtures.PROLOGUE;
            final UUID idThatDoesNotExists = UUID.fromString("86e03298-367e-48f9-afa8-2d90438f4d2b");

            /* When */
            final Optional<Chapter.EntryIndex> result = chapter.findEntryWithId(idThatDoesNotExists);

            /* Then */
            assertThat(result)
                    .isEmpty();
        }
    }

    @Nested
    class InsertEntryAtTest {

        @Test
        void insertEntryInAnEmptyChapter() {
            /* Given */
            final Chapter chapter = Chapter.builder().chapterPath("chapter-path.json").build();
            final Entry entry = Entry.builder().id(UUID.randomUUID()).build();

            /* When */
            final Chapter result = chapter.insertEntryAt(0, entry);

            /* Then */
            assertThat(result)
                    .isEqualTo(Chapter.builder()
                            .chapterPath("chapter-path.json")
                            .entry(entry)
                            .build());
        }

        @Test
        void insertsEntryAtGivenIndex() {
            /* Given */
            final Chapter chapter = Fixtures.PROLOGUE;
            final Entry entry = Entry.builder().id(UUID.randomUUID()).build();

            /* When */
            final Chapter result = chapter.insertEntryAt(1, entry);

            /* Then */
            assertThat(result)
                    .isEqualTo(Chapter.builder()
                            .chapterPath("00-prologue.json")
                            .entry(Fixtures.PROLOGUE_ENTRY_1)
                            .entry(entry)
                            .entry(Fixtures.PROLOGUE_ENTRY_2)
                            .build());
        }

        @Test
        void insertsEntryAtTheEnd() {
            /* Given */
            final Chapter chapter = Fixtures.PROLOGUE;
            final Entry entry = Entry.builder().id(UUID.randomUUID()).build();

            /* When */
            final Chapter result = chapter.insertEntryAt(2, entry);

            /* Then */
            assertThat(result)
                    .isEqualTo(Chapter.builder()
                            .chapterPath("00-prologue.json")
                            .entry(Fixtures.PROLOGUE_ENTRY_1)
                            .entry(Fixtures.PROLOGUE_ENTRY_2)
                            .entry(entry)
                            .build());
        }
    }
}
