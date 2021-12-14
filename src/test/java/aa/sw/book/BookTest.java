package aa.sw.book;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BookTest {

    @Test
    void throwsNullPointerExceptionWhenCreatedWithoutTitle() {
        /* Given */
        final Book.BookBuilder builder = Book.builder().description("description");

        /* When/Then */
        assertThatThrownBy(builder::build)
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void throwsNullPointerExceptionWhenCreatedWithoutDescription() {
        /* Given */
        final Book.BookBuilder builder = Book.builder().title("title");

        /* When/Then */
        assertThatThrownBy(builder::build)
                .isInstanceOf(NullPointerException.class);
    }
}
