package aa.sw.book;

import aa.sw.common.Result;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class BookDataTest {

    private final ObjectMapper mapper = createObjectMapper();
    private final BookData bookData = new BookData(mapper);

    @Test
    void readBook() {
        /* Given */
        final Path bookPath = Path.of("src/test/resources/fixtures/books/book.json");

        /* When */
        final Result<Book> result = bookData.readBook(bookPath);

        /* Then*/
        assertThat(result).isEqualTo(Result.value(Fixtures.BOOK));
    }

    private static ObjectMapper createObjectMapper() {
        /* TODO: This is not necessary the same used by Spring */
        return JsonMapper.builder()
                .addModule(new Jdk8Module())
                .addModule(new JavaTimeModule())
                .serializationInclusion(JsonInclude.Include.NON_NULL)
                .visibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
                .visibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
                .build();
    }
}