package aa.sw.book;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class ChapterTest {

    @Nested
    class EntryTest {

        @ParameterizedTest
        @ValueSource(strings = { "command", "create", "docker-tag-and-push", "download", "git-apply-patch",
                "git-commit-changes", "git-tag-current-commit", "replace" })
        void isRunnableTrueForRunnableEntries(final String type) {
            /* Given */
            final Chapter.Entry entry = Chapter.Entry.builder().type(type).build();

            /* When */
            final boolean isRunnable = entry.isRunnable();

            /* Then */
            assertThat(isRunnable)
                    .describedAs("")
                    .isTrue();
        }
    }
}
