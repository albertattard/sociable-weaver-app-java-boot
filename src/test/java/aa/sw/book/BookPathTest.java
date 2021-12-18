package aa.sw.book;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

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

    @Test
    void returnTheGivenPathAsStringWhenItIsNotADirectory() {
        /* Given */
        final Path aPathThatIsNotADirectory = Path.of("not-a-directory");
        final BookPath bookPath = BookPath.of(aPathThatIsNotADirectory);

        /* When */
        final String path = bookPath.toString();

        /* Then */
        assertThat(path)
                .isEqualTo("not-a-directory");
    }
}