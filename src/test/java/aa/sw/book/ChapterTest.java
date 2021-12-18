package aa.sw.book;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ChapterTest {

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
}
