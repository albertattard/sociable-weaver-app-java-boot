package aa.sw.book;

import aa.sw.config.JacksonConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class BookServiceTest {

    @Test
    void openBook() {
        final ObjectMapper mapper = new JacksonConfiguration().createObjectMapper();
        final BookService service = new BookService(mapper);

        final Result<Book> book = service.openBook(Path.of("somewhere"));

        assertThat(book)
                .describedAs("")
                .isEqualTo(Result.value(Book.builder()
                        .title("Programming")
                        .description("A book about programming")
                        .chapter("Prologue", "The prologue", "00-prologue.json")
                        .chapter("Hello World", "Automation", "01-hello-world.json")
                        .chapter("Broken Links", "Test Driven Development", "02-broken-links.json")
                        .build()));
    }
}
