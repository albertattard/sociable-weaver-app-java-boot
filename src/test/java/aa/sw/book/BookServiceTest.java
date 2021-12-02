package aa.sw.book;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BookServiceTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private final BookService service = new BookService(mapper);

    @Nested
    class OpenBookTest {

        @Test
        void openBook() {

            final Result<Book> book = service.openBook(Path.of("path-to-book"));

            assertThat(book)
                    .isEqualTo(Result.value(Book.builder()
                            .title("Programming")
                            .description("A book about programming")
                            .chapter("Prologue", "The prologue", "00-prologue.json")
                            .chapter("Hello World", "Automation", "01-hello-world.json")
                            .chapter("Broken Links", "Test Driven Development", "02-broken-links.json")
                            .build()));
        }
    }

    @Nested
    class ReadChapterTest {

        @Test
        void readChapter() {
            final Result<Chapter> chapter = service.readChapter(Path.of("path-to-chapter"));

            assertThat(chapter)
                    .isEqualTo(Result.value(Chapter.builder()
                            .entry(Chapter.Entry.builder()
                                    .type("chapter")
                                    .id(UUID.fromString("3a50daae-ab81-426f-a118-b505e7eecb49"))
                                    .parameters(List.of("Prologue"))
                                    .build())
                            .entry(Chapter.Entry.builder()
                                    .type("markdown")
                                    .id(UUID.fromString("483214f8-fc66-4a3a-b8dc-26401ac6a608"))
                                    .parameters(List.of("We make mistakes, and we make more mistakes, and some more, and that's how we learn.")).build())
                            .build()));
        }
    }
}
