package aa.sw.book;

import aa.sw.common.Result;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class BookPathTest {

    @Test
    void openSpecificBook() {
        /* Given */
        final BookPath bookPath = BookPath.of(Fixtures.resolve("book.json"));

        /* When */
        final Path path = bookPath.getPath();

        /* Then */
        assertThat(path)
                .isEqualTo(Fixtures.resolve("book.json"));
    }

    @Test
    void openBookFoundInFolder() {
        /* Given */
        final BookPath bookPath = BookPath.of(Fixtures.BOOK_DIRECTORY);

        /* When */
        final Path path = bookPath.getPath();

        /* Then */
        assertThat(path)
                .isEqualTo(Fixtures.resolve("book.json"));
    }
}